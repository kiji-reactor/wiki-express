/**
 * (c) Copyright 2013 WibiData, Inc.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kiji.express.examples.wikimedia

import java.io.IOException
import java.io.InputStream

import scala.collection.JavaConversions._
import scala.io.Source

import cascading.pipe.joiner._
import com.twitter.scalding._
import com.twitter.scalding.mathematics.Matrix._
import opennlp.tools.tokenize.Tokenizer
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel

import com.wibidata.wikimedia.avro.RevMetaData

import org.kiji.express.KijiSlice
import org.kiji.express.flow.all
import org.kiji.express.flow.Column
import org.kiji.express.flow.KijiInput
import org.kiji.express.flow.KijiJob
import org.kiji.express.wikimedia.util.RevisionDelta
import org.kiji.express.wikimedia.util.RevisionDelta.Operation
import org.kiji.express.wikimedia.util.RevisionDelta.Operation.Operator

/**
 * Calculate the term frequency (tf) and inverse document frequency (idf) of each unique
 * word across all reverted edits.
 *
 * This job accepts three command line arguments, `--page-table`, `--revision-table`, and
 * '--user-table', which should be set to the Kiji URIs of their respective tables in Kiji.
 * The text of each reverted edit (stored in column 'delta_no_templates' of table 'revision')
 * is used to compute the most frequent, pertinent words across all reverted edits, using the
 * tf-idf algorithm. The tf-idf score for each song is written to the column
 * 'derived:tf_idf' of the revision table.
 *
 *@param args passed from the command line.
 */
class MutualInfo(args: Args) extends KijiJob(args) {
  /**
   * Filters a slice of all edits made by a user for only reverted edits.
   *
   * @param fields is a tuple of size 2 containing the 'revision and 'isReverted fields
   *     from the Scalding flow.
   * @return a sequence of strings, each representing a raw reverted edit.
   */
  def filterForReverted(fields: Tuple2[Seq[String], Seq[Boolean]]): Seq[String] = {
    val revisionSeq: Seq[String] = fields._1
    val isRevertedSeq: Seq[Boolean] = fields._2
    val toFilter: Seq[(String, Boolean)] = revisionSeq.zip(isRevertedSeq)

    toFilter.flatMap {
      x: (String, Boolean) => {
        val revision = x._1
        val isReverted = x._2
        if (isReverted == true) { // TODO can isReverted be null (java Boolean obj)?
          Some(revision)
        } else {
          None
        }
      }
    }
  }

  /**
   * Filters a slice of all edits made by a user for non-reverted, non-reverting edits.
   *
   * @param fields is a tuple of size 3 containing the 'revision, 'isReverted, and
   *     'isReverting fields from the Scalding flow.
   * @return a sequence of strings, each representing a raw unreverted edit.
   */
  def filterForUnreverted(fields: Tuple3[Seq[String], Seq[Boolean], Seq[Boolean]]): Seq[String] = {
    val revisionSeq: Seq[String] = fields._1
    val isRevertedSeq: Seq[Boolean] = fields._2
    val isRevertingSeq: Seq[Boolean] = fields._3
    val toRemove: Seq[Boolean] = isRevertedSeq ++ isRevertingSeq // Union of sequences.
    val toFilter: Seq[(String, Boolean)] = revisionSeq.zip(toRemove)

    toFilter.flatMap {
      x: (String, Boolean) => {
        val revision = x._1
        val toRemove = x._2
        if (!toRemove) {
          Some(revision)
        } else {
          None
        }
      }
    }
  }

  /**
   * Tokenizes the raw text of a given edit.
   *
   * @param stringDelta is the raw text, in +/-/= diff format, of an edit.
   * @return a sequence of words representing the tokenized edit.
   */
  def tokenizeWords(stringDelta: String): Seq[String] = {
    val delta = new RevisionDelta(stringDelta)

    // Get the raw text of insertions and deletions, and combine them into one string.
    var rawText = ""
    val deltaIter = delta.iterator()
    deltaIter.foreach { op: Operation =>
      if (Operator.INSERT == op.getOperator) {
        rawText += (op.getText + " ")
      } else if (Operator.DELETE == op.getOperator) {
        rawText += (op.getOperand + " ")
      }
    }

    // Tokenizes the raw text using OpenNLP and returns to the flatMap
    // a sequence where each element is one word in the edit.
    val modelIn: InputStream = getClass.getClassLoader
        .getResourceAsStream("org/kiji/express/wikimedia/en-token.bin")
    var tokenized: Seq[String] = Seq()
    try {
      val model: TokenizerModel = new TokenizerModel(modelIn)
      val tokenizer: Tokenizer = new TokenizerME(model)
      tokenized = tokenizer.tokenize(rawText).toSeq
    } catch {
      case e: IOException => e.printStackTrace()
    } finally {
      if (modelIn != null) {
        try {
          modelIn.close()
        } catch {
          case e: IOException => e.printStackTrace()
        }
      }
    }

    tokenized
  }

  /**
   * Gets the page id out of the Avro record for revision metadata which corresponds
   * to each reverted edit.
   *
   * @param slice that records the metadata for all reverted edits made by a user,
   *     from the column 'info:meta_data'.
   * @return a sequence of page id's.
   */
  def getPageIds(slice: KijiSlice[RevMetaData]): Seq[Long] = {
    slice.cells.map { cell => cell.datum.getPageId.asInstanceOf[Long] }
  }

  /**
   * Given a document as a sequence of words, counts the number of occurrences of
   * each word in the document and returns these counts as a sequence of tuples
   * representing the document-level word count for every word in the document.
   *
   * @param document of tokenized words from every reverted edit on a given page.
   * @return a sequence of (word, count) tuples, where 'count' is the number of times
   * the given word occurs in this document.
   */
  def countWordInDoc(document: List[String]): Seq[(String, Double)] = {
    document.groupBy(x => x)
        .mapValues(x => x.length.toDouble)
        .map { case (k, v) => (k, v) }(collection.breakOut): Seq[(String, Double)]
  }

  /**
   * Calculates the base-2 logarithm of a number.
   *
   * @param x is the number used to compute log_2(x).
   * @return the result of the logarithm operation.
   */
  def log2(x: Double): Double = scala.math.log(x)/scala.math.log(2.0)

  /**
   * Calculate the mutual information of a word, given pre-calculated N-counts.
   * Based on <a href="http://nlp.stanford.edu/IR-book/html/htmledition/mutual-information-1.html">
   * Equation 131</a> from Manning, Raghavan, & Schutze's <emphasis>Introduction to
   * Information Retrieval</emphasis>.
   *
   * @param fields is a tuple of size 5 containing the 'n11, 'n01, 'n10, 'n00, and 'totalN
   *     fields from the Scalding flow.
   * @return a double representing a word's mutual information bits.
   */
  def calculateMI(fields: Tuple5[Double, Double, Double, Double, Double]): Double = {
    val n11 = fields._1
    val n01 = fields._2
    val n10 = fields._3
    val n00 = fields._4
    val totalN = fields._5

    // Calculate each term of the MI equation.
    val t1 = (n11 / totalN) * log2( (totalN * n11) / ((n10 + n11) * (n01 + n11)) )
    val t2 = (n01 / totalN) * log2( (totalN * n01) / ((n00 + n01) * (n01 + n11)) )
    val t3 = (n10 / totalN) * log2( (totalN * n10) / ((n10 + n11) * (n00 + n10)) )
    val t4 = (n00 / totalN) * log2( (totalN * n00) / ((n00 + n01) * (n00 + n10)) )

    val mutualInfo = t1 + t2 + t3 + t4
    return mutualInfo
  }
  /**
   * This Scalding pipeline processes reverted edits by doing the following:
   * 1. Reads slices of necessary columns from the 'revision' Kiji table.
   * 2. Filters for only reverted edits out of each Kiji slice.
   * 3. Tokenizes the text of each reverted edit.
   * 4. Gets the page ids from the metadata for each reverted edit.
   * 5. Groups the edits by page id as lists of word sequences.
   * 6. Flattens the individual edits into a single list of words per page (a 'document').
   * 7. Calculates the term frequency of each word in a given document as (word, count) tuples.
   * 8. Flattens the (word, count) tuples into separate fields.
   */
  val revDocs = KijiInput(args("revision-uri"))(Map(
      Column("info:delta_no_templates", all) -> 'revision,
      Column("info:meta_data", all) -> 'metadata,
      Column("derived:is_reverted", all) -> 'isReverted))
      .flatMapTo(('revision, 'isReverted) -> 'revertedEdit ) { filterForReverted }
      .mapTo('revertedEdit -> 'edit) { tokenizeWords } // Each 'edit is of type Seq[String].
      .flatMapTo('metadata -> 'pageId) { getPageIds }
      .groupBy('pageId) { _.toList[Seq[String]]('edit -> 'page) }
      .flatMap('page -> 'page) { x: String => x }
      .flatMap('page -> 'tfTuple) { countWordInDoc }
      .map('tfTuple -> ('word, 'tfCount)) { x: (String, Double) => (x._1, x._2) }

  /**
   * This Scalding pipeline processes unreverted edits by doing the following:
   * 1. Reads slices of necessary columns from the 'revision' Kiji table.
   * 2. Filters for only unreverted (non-reverted, non-reverting) edits out of each Kiji slice.
   * 3. Tokenizes the text of each unreverted edit.
   * 4. Gets the page ids from the metadata for each unreverted edit.
   * 5. Groups the edits by page id as lists of word sequences.
   * 6. Flattens the individual edits into a single list of words per page (a 'document').
   * 7. Calculates the term frequency of each word in a given document as (word, count) tuples.
   * 8. Flattens the (word, count) tuples into separate fields.
   */
  val unrevDocs = KijiInput(args("revision-uri"))(Map(
      Column("info:delta_no_templates", all) -> 'revision,
      Column("info:meta_data", all) -> 'metadata,
      Column("derived:is_reverted", all) -> 'isReverted,
      Column("derived:is_reverting", all) -> 'isReverting))
      .flatMapTo(('revision, 'isReverted, 'isReverting) -> 'unrevertedEdit ) { filterForUnreverted }
      .mapTo('unrevertedEdit -> 'unrevEdit) { tokenizeWords } // Each 'edit is of type Seq[String].
      .flatMapTo('metadata -> 'pageId) { getPageIds }
      .groupBy('pageId) { _.toList[Seq[String]]('edit -> 'page) }
      .flatMap('page -> 'page) { x: String => x }
      .flatMap('page -> 'tfTuple) { countWordInDoc }
      .map('tfTuple -> ('word, 'tfCount)) { x: (String, Double) => (x._1, x._2) }

  // Creates document-to-word matrices for both reverted and unreverted edits, where
  // m[i, j] = term frequency of word j in document i.
  // Note: these two matrices do not have the same dimensions!
  val revTfMatrix = revDocs.toMatrix[Long, String, Double]('pageId, 'word, 'tfCount)
  val unrevTfMatrix = unrevDocs.toMatrix[Long, String, Double]('pageId, 'word, 'tfCount)

  // Creates equivalent binary matrices with value 1 for each document that contains a given word.
  val revCountMatrix = revTfMatrix.binarizeAs[Double]
  val unrevCountMatrix = unrevTfMatrix.binarizeAs[Double]

  // Computes the document frequency (df) for each word in both corpora.
  val revDfVector = revCountMatrix.sumRowVectors // N_11 values.
  val unrevDfVector = unrevCountMatrix.sumRowVectors // N_10 values.

  // Join the N-count vectors for each class into two pipes by word.
  val revDfPipe = revDfVector.toMatrix(1)
      .pipeAs('pageId, 'word, 'n11)
      .discard('pageId)
  val invRevDfPipe = invRevDfVector.toMatrix(1)
      .pipeAs('pageId, 'word, 'n01)
      .discard('pageId)
  val revertedPipe = revDfPipe.joinWithLarger('word -> 'word, invRevDfPipe)

  val unrevDfPipe = unrevDfVector.toMatrix(1)
      .pipeAs('pageId, 'word, 'n10)
      .discard('pageId)
  val invUnrevDfPipe = invUnrevDfVector.toMatrix(1)
      .pipeAs('pageId, 'word, 'n00)
      .discard('pageId)
  val unrevertedPipe = unrevDfPipe.joinWithLarger('word -> 'word, invUnrevDfPipe)

  // N = total number of documents, independent of class.
  // The size of each df vector is the number of total documents in that class.
  revDfPipe.groupAll { _.size }
      .write(Tsv(args("revDfSize")))
  val revDfSize: Double = Source.fromFile("revDfSize").getLines.mkString.toDouble
  unrevDfPipe.groupAll { _.size }
      .write(Tsv(args("unrevDfSize")))
  val unrevDfSize: Double = Source.fromFile("unrevDfSize").getLines.mkString.toDouble
  val totalN = revDfSize + unrevDfSize

  // Computes the number of documents that do _not_ contain a given word.
  val invRevDfVector = revDfVector.toMatrix(1)
      .mapValues { df => revDfSize - df }
      .getRow(1) // N_01 values.
  val invUnrevDfVector = unrevDfVector.toMatrix(1)
      .mapValues { df => unrevDfSize - df}
      .getRow(1) // N_00 values.

  // Join the reverted-edit and unreverted-edit word pipes to combine all 4 N-counts,
  // then add the total document count (N) as a constant field.
  val mutualInfoPipe = revertedPipe.joinWithLarger('word -> 'word, unrevertedPipe,
      joiner = new OuterJoin)
  mutualInfoPipe.insert('totalN, totalN)

  // Calculate the mutual information (MI) of each word, finally!
  mutualInfoPipe.map(('n11, 'n01, 'n10, 'n00, 'totalN) -> 'mutualInfo) { calculateMI }

  // Output the top 10 words by highest MI.
  mutualInfoPipe.project('word, 'mutualInfo)
      .map('mutualInfo -> 'sort) { x: Long => x * (-1) }
      .groupAll { _.sortBy('sort) }
      .limit(10)
      .discard('sort)
      .write(Tsv(args("output")))

}
