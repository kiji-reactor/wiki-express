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

import java.io.InputStreamReader
import java.util.Scanner

import scala.collection.mutable.Buffer

import com.twitter.scalding._

import org.kiji.express._
import org.kiji.express.flow._

/**
 * A test for counting the number of times songs have been played by users.
 */
class StopWordsSuite extends KijiSuite {

  // Make a Kiji table with the appropriate layout and get its URI.
  // test against.
  val tableLayout = layout("revision.json") // located in src/test/resources
  val table = makeTestKijiTable(tableLayout, "stopwords")
  val tableURI = table.getURI.toString

  // Make a new testing table for EntityId's with only one component.
//  val tableLayout = layout("revision_1eid.json") // located in src/test/resources
//  val table = makeTestKijiTable(tableLayout, "stopwords")
//  val tableURI = table.getURI.toString

  // Create some test data of reverted revisions.
  val text0 = "+Testing testing 123! testing"
  val isr: InputStreamReader =
      new InputStreamReader(ClassLoader.getSystemResourceAsStream("wiki_critical_theory.txt"))
  val scanner = new Scanner(isr).useDelimiter("\\A")
  val text1: String = scanner.next()

  val testInput =
      (EntityId(1L, 123L),
          slice("info:delta_no_templates", (0L, text0))) ::
      (EntityId(1L, 123L),
          slice("info:delta_no_templates", (0L, text1))) ::
      (EntityId(2L, 123L),
          slice("info:delta_no_templates", (0L, text1))) ::
      (EntityId(3L, 123L),
          slice("info:delta_no_templates", (0L, text1))) ::
      (EntityId(1L, 123L), slice("revert_type:is_reverted", (0L, true))) ::
      (EntityId(2L, 123L), slice("revert_type:is_reverted", (0L, true))) ::
      (EntityId(3L, 123L), slice("revert_type:is_reverted", (0L, true))) ::
      Nil
  val testInput_oneCol =
      (EntityId(1L, 123L),
          slice("info:delta_no_templates", (0L, text0))) ::
      (EntityId(2L, 123L),
          slice("info:delta_no_templates", (0L, text0))) ::
      (EntityId(3L, 123L),
          slice("info:delta_no_templates", (0L, text0))) ::
      Nil
  val testInput_fullRow =
      (EntityId(1L, 123L),
          slice("info:delta_no_templates", (0L, text0))) ::
      (EntityId(1L, 123L), slice("revert_type:is_reverted", (0L, true))) ::
      (EntityId(1L, 123L), slice("info:comment", (0L, "testing"))) ::
      Nil

  // Test input for EntityId's with only one component.
  val testInput_1eid =
    (EntityId(1L), slice("info:delta_no_templates", (0L, text0))) ::
    (EntityId(1L), slice("revert_type:is_reverted", (0L, true))) ::
    (EntityId(1L), slice("info:comment", (0L, "testing"))) ::
    Nil

  val outputPath = "/home/lisa/src/wiki-express/src/test/resources/StopWordsSuiteOutput.txt"

  /**
   * Validates that the job can print out data after tokenization.
   *
   * @param tokenizedEdits A buffer of string sequences representing each tokenized word
   *     in a given edit.
   */
  def validateTokenize(tokenizedEdits: Buffer[Seq[String]]) {
    System.out.println("validating tokenize:")
    tokenizedEdits.map {
      edit: Seq[String] => {
        assert(edit(0) === "Testing")
        assert(edit(1) === "testing")
        assert(edit(2) === "123")
        assert(edit(3) === "!")
        assert(edit(4) === "testing")
      }
    }

  }

  /**
   * Validates that the job output is of the expected length.
   *
   * @param top10Words A buffer of two Scalding tuples representing the word and
   *     word count for the top 10 most frequent words across all rows,
   */
  def validateOutput(top10Words: Buffer[(String, Double)]) {
    val numLines = top10Words.toSeq.size
    assert(10 === numLines, "Ten lines of word counts were expected.")
    top10Words.map { x: (String, Double) => System.out.println(x) }
  }

  test("StopWords can output a list of tokenized edits from one column.") {
    JobTest(new StopWords(_))
        .arg("revision-uri", tableURI)
        .arg("output", outputPath)
        .source(KijiInput(tableURI)(
            "info:delta_no_templates" -> 'revision),
            testInput_oneCol)
        .sink(Tsv("output")) { validateTokenize }
        .run
        .finish
  }

//  test("StopWords can output a file of the top 10 most frequent words.") {
//    JobTest(new StopWords(_))
//        .arg("revision-uri", tableURI)
//        .arg("output", outputPath)
//        .source(KijiInput(tableURI)(
//            "info:delta_no_templates" -> 'revision,
//            "revert_type:is_reverted" -> 'isReverted),
//            testInput)
//        .sink(Tsv("output")) { validateOutput }
//        .run
//        .finish
//  }

}
