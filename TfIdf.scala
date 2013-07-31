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

import scala.collection.JavaConverters._

import chalk.text.tokenize.SimpleEnglishTokenizer
import chalk.text.transform._
import com.twitter.scalding._
import com.twitter.scalding.mathematics.Matrix._
import com.google.common.collect.Lists

import org.kiji.express._
import org.kiji.express.KijiJob
import org.kiji.express.DSL._
import scala.collection.parallel.mutable
import scala.collection

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
class TfIdf(args: Args) extends KijiJob(args) {
  /**
   * Filters a slice of all edits made by a user for only reverted edits.
   *
   * @param fields is a tuple of size 2 containing the 'revision and 'isReverted fields
   *     from the Scalding flow.
   * @return a sequence of strings, each representing a raw reverted edit.
   */
  def filterForReverted(fields): Seq[String] = {
    val revisionSeq: Seq[String] = fields._1.cells
    val isRevertedSeq: Seq[Boolean] = fields._2.cells
    val toFilter: Seq[(String, Boolean)] = revisionSeq.zip(isRevertedSeq)

    toFilter.map {
      x: (String, Boolean) => {
        val revision = x._1
        val isReverted = x._2
        if (isReverted) {
          revision
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
    rawText += {
      delta.foreach { op =>
        if (Operator.INSERT == op.getOperator) {
          op.getText
        }
        else if (Operator.DELETE == op.getOperator) {
          op.getOperand
        }
      }
    }

    // Parse text with Chalk (formerly Breeze) and pass to a sequence with each item
    // representing one word in the edit.
    val iter = new SimpleEnglishTokenizer(rawText)
    val filteredIter: Iterable[String] = iter.filter(new StopWordFilter("en"))
    filteredIter.toSeq()
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
    //TODO figure out how to import Avro type com.wibidata.wikimedia.avro.RevMetaData
    slice.cells.map { cell => cell.datum.getPageId }
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
    document.groupBy(x => x).mapValues(x => x.length).toSeq()
  }

  /**
   * Calculates the base-2 logarithm of a number.
   *
   * @param x is the number used to compute log_2(x).
   * @return the result of the logarithm operation.
   */
  def log2(x: Double) = scala.math.log(x)/scala.math.log(2.0)

  /**
   * This Scalding pipeline does the following:
   * 1. Reads slices of necessary columns from the 'revision' Kiji table.
   * 2. Filters for only reverted edits out of each Kiji slice.
   * 3. Tokenizes the text of each reverted edit.
   * 4. Gets the page ids from the metadata for each reverted edit.
   * 5. Groups the edits by page id as lists of word sequences.
   * 6. Flattens the individual edits into a single list of words per page (a 'document').
   * 7. Calculates the term frequency of each word in a given document as (word, count) tuples.
   * 8. Flattens the (word, count) tuples into separate fields.
   */
  val pageDocs = KijiInput(args("revision-uri"))(Map(
      Column("info:delta_no_templates", all) -> 'revision,
      Column("info:meta_data", all) -> 'metadata,
      Column("derived:is_reverted", all) -> 'isReverted))
      .flatMapTo(('revision, 'isReverted) -> 'revertedEdit ) { filterForReverted }
      .mapTo('revertedEdit -> 'edit) { tokenizeWords } // Each 'edit is of type Seq[String].
      .flatMapTo('metadata -> 'pageId) { getPageIds }
      .groupBy('pageId) { _.toList[Seq[String]]('edit -> 'page) }
      .map('page -> 'page) { _.flatten }
      .flatMap('page -> 'tfTuple) { countWordInDoc }
      .map('tfTuple -> ('word, 'tfCount)) { x => (x._1, x._2) }

  // Create a document-to-word matrix where m[i, j] = term frequency of word j in document i.
  // Based on the Scalding example MatrixTutorial6.
  val tfMatrix = pageDocs.toMatrix[Long, String, Double]('pageId, 'word, 'tfCount)

  // Compute the inverse document frequency (idf) for each word (row).
  val dfVector = tfMatrix.binarizeAs[Double].sumRowVectors()
  val totalDocs = dfVector.size // Needed to calculate idf.
  val idfVector = dfVector.toMatrix(1).mapValues( x => log2(totalDocs / x) ).getRow(1)
  val idfMatrix = tfMatrix.zip(idfVector).mapValues( pair => pair._2 )

  // Compute tf-idf for each word in the matrix.
  val tfIdfMatrix = tfMatrix.hProd(idfMatrix)

  // Sum the tf-idf scores for a given word across all documents (a 'word feature vector')
  // and output the top 10 words.
  val tfIdfWordVector = tfIdfMatrix.sumColVectors()
  tfIdfWordVector.toMatrix(1)
      .topRowElems(10)
      .pipe
      .write(Tsv(args("output")))

}
