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

import scala.collection.mutable.Buffer
import scala.io.Source

import com.twitter.scalding._

import org.kiji.express._
import org.kiji.express.flow._

/**
 * A test for counting the number of times songs have been played by users.
 */
class StopWordsSuite extends KijiSuite {

  // Get a Kiji to use for the test and record the Kiji URI of the users and songs tables we'll
  // test against.
  val tableLayout = layout("revision.json") // located in src/test/resources
  val table = makeTestKijiTable(tableLayout, "stopwords")

  val tableURI = table.getURI.toString

  // Create some test data for three reverted revisions.
  val text0 = "Testing testing 123! testing"
//  val text1 = Source.fromFile("/home/lisa/src/wiki-express/src/test/resources/" +
//      "wiki_critical_theory.txt").getLines().mkString
//  val text2 = Source.fromFile("/home/lisa/src/wiki-express/src/test/resources/" +
//      "wiki_meme.txt").getLines().mkString
//  val text3 = Source.fromFile("/home/lisa/src/wiki-express/src/test/resources/" +
//      "wiki_semiotics.txt").getLines().mkString
  val testInput =
      (EntityId(1L, 123L),
          slice("info:delta_no_templates", (0L, text0))) ::
//      (EntityId(1L, 123L),
//          slice("info:delta_no_templates", (0L, text1))) ::
//      (EntityId(2L, 123L),
//          slice("info:delta_no_templates", (0L, text2))) ::
//      (EntityId(3L, 123L),
//          slice("info:delta_no_templates", (0L, text3))) ::
//      (EntityId(1L, 123L), slice("revert_type:is_reverted", (0L, true))) ::
//      (EntityId(2L, 123L), slice("revert_type:is_reverted", (0L, true))) ::
//      (EntityId(3L, 123L), slice("revert_type:is_reverted", (0L, true))) ::
      Nil

  /**
   * Validates that the job output is of the expected length.
   *
   * @param top10Words contains two tuples representing the word and word count for the top 10
   *     most frequent words across all rows,
   */
  def validateOutputLength(top10Words: Buffer[(String, Double)]) {
    val numLines = top10Words.toSeq.size
    assert(10 === numLines, "Ten lines of word counts were expected.")
  }

  test("StopWords TESTING.") {
    JobTest(new StopWords(_))
        .arg("revision-uri", tableURI)
        .source(KijiInput(tableURI)(Map(
        Column("info:delta_no_templates", all) -> 'revision)),
        testInput)
        .sink(Tsv("output")) { validateOutputLength }
        .run
        .finish
  }

//  test("StopWords can output a file of the top 10 most frequent words.") {
//    val outputPath = "/home/lisa/src/wiki-express/src/test/resources/StopWordsSuiteOutput.txt"
//    JobTest(new StopWords(_))
//        .arg("revision-uri", tableURI)
//        .arg("output", outputPath)
//        .source(KijiInput(tableURI)(Map(
//            Column("info:delta_no_templates", all) -> 'revision,
//            Column("revert_type:is_reverted", all) -> 'isReverted)),
//            testInput)
//        .sink(Tsv("output")) { validateOutputLength }
//        .run
//        .finish
//  }
}
