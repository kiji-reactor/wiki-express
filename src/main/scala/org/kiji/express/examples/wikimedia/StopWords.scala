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

import chalk.text.tokenize.SimpleEnglishTokenizer
import chalk.text.transform._
import scala.collection.JavaConversions._
import com.twitter.scalding._
import com.twitter.scalding.mathematics.Matrix._

import org.kiji.express.Cell
import org.kiji.express.KijiSlice
import org.kiji.express.flow.all
import org.kiji.express.flow.Column
import org.kiji.express.flow.KijiInput
import org.kiji.express.flow.KijiJob
import org.kiji.express.wikimedia.util.RevisionDelta
import org.kiji.express.wikimedia.util.RevisionDelta.Operation
import org.kiji.express.wikimedia.util.RevisionDelta.Operation.Operator

/**
 * Count the number of times that each unique word appears in the corpus of reverted edits in
 * Wikipedia, excluding stopwords.
 *
 * This job accepts one command line argument, '--revision-table`, which should be set to the
 * Kiji URIs of the 'revision' table in Kiji. The text of each reverted edit (stored in column
 * 'info:delta_no_templates' of table 'revision') is used to compute the 100 most frequent
 * unique words across all reverted edits, excluding those words on the stopwords list.
 * These words are written to a tab-delimited file on HDFS, at the filepath given by the
 * argument '--output'.
 *
 * @param args passed in from the command line.
 */
class StopWords(args: Args) extends KijiJob(args) {
  // List of English stopwords borrowed from Lucene.
  /* TODO remove if I don't end up using
  val mStopwords = Set("a", "an", "and", "are", "as", "at", "be", "but", "by", "for",
      "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the",
      "their", "then", "there", "these", "they", "this", "to", "was", "will", "with")
  */

  /**
   * Gets the full text of all reverted edits made by a user.
   *
   * @param fields is a tuple of size 2 containing the 'revision and 'isReverted fields
   *     from the Scalding flow.
   * @return a sequence of Kiji slices containing the revision if it is a reverted edit.
   */
  def filterForReverted(fields: Tuple2[KijiSlice[String], KijiSlice[Boolean]]):
      Seq[KijiSlice[String]] = {
    val revision: KijiSlice[String] = fields._1
    val isRevertedSlice: KijiSlice[Boolean] = fields._2
    isRevertedSlice.cells.flatMap {
      isReverted: Cell[Boolean] => {
        if (isReverted.datum == true) {
          Some(revision)
        } else {
          None
        }
      }
    }
  }

  /**
   * Gets the full text of all reverted edits made by a user.
   *
   * @param slice that records all reverted edits made by a user, from the column
   *     'info:delta_no_templates'.
   * @return a sequence of all the words in this slice.
   */
  def tokenizeWords(slice: KijiSlice[String]): Seq[String] = {
    slice.cells.flatMap {
      cell: Cell[String] => {
        // Get the content of each reverted edit.
        val stringDelta = cell.datum
        val delta = new RevisionDelta(stringDelta)

        // Get the raw text of insertions and deletions, and combine them into one string.
        var rawText = ""
        rawText += {
          val iter = delta.iterator()
          iter.flatMap { op: Operation =>
            if (Operator.INSERT == op.getOperator) {
              Some(op.getText)
            } else if (Operator.DELETE == op.getOperator) {
              Some(op.getOperand)
            } else {
              None
            }
          }
        }

        // Parse text with Chalk (formerly Breeze) and pass to a sequence with each item
        // representing one word in the edit.
        val tokenizer = SimpleEnglishTokenizer()
        val iter: Iterable[String] = tokenizer(rawText)
        val stopWordFilter = StopWordFilter("en")
        val filteredIter: Iterable[String] = stopWordFilter(iter)
        filteredIter.toSeq
      }
    }
  }

  val testPipe = KijiInput(args("revision-uri"))(Map(
      Column("info:delta_no_templates", all) -> 'revision))
      .flatMapTo('revision -> 'word) { tokenizeWords }
      .write(Tsv("output"))

  /**
   * This Scalding pipeline does the following:
   * 1. Reads the columns "info:delta_no_templates" and "revert_type:is_reverted" from
   *    a Kiji table.
   * 2. Tokenizes the text of each reverted edit into a list of words.
   * 3. Counts the number of times each word occurs, across all rows.
   * 4. Converts the pipe to a matrix and limits to the 10 highest word counts.
   * 5. Converts the matrix back to a pipe with the appropriate tuple field names.
   * 6. Discards the constant 'count' field created by the prior matrix conversion.
   * 7. Writes each of the top 10 words by word count to an output file in HDFS.
   */
//  val wordCountPipe = KijiInput(args("revision-uri"))(Map(
//      Column("info:delta_no_templates", all) -> 'revision,
//      Column("revert_type:is_reverted", all) -> 'isReverted))
//      .flatMapTo(('revision, 'isReverted) -> 'revertedEdit ) { filterForReverted }
//      .flatMapTo('revertedEdit -> 'word) { tokenizeWords }
//      .groupBy('word) { _.size('wordCount) }
//
//  wordCountPipe.toMatrix[String, String, Double]("count", 'word, 'wordCount)
//      .topRowElems(10)
//      .pipeAs('count, 'word, 'wordCount)
//      .discard('count)
//      .write(Tsv(args("output")))
}
