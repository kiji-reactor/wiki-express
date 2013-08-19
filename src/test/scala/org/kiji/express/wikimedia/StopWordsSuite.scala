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

import com.twitter.scalding._

import org.kiji.express._
import org.kiji.express.flow._

/**
 * A test for counting the number of times songs have been played by users.
 */
class StopWordsSuite extends KijiSuite {

  // Get a Kiji to use for the test and record the Kiji URI of the users and songs tables we'll
  // test against.
  val kiji = makeTestKiji("StopWordsSuite")
  val revisionURI = kiji.getURI().toString + "/revision"

  // Execute the DDL shell commands in music-schema.ddl to create the tables for the music
  // tutorial.
  executeDDLResource(kiji, "/home/lisa/src/wiki-express/src/test/resources/revision.ddl")

  // Create some test data for three reverted revisions.
  val text1 = ;
  val text2;
  val text3;
  val testInput =
      (EntityId("123"),
          slice("info:delta_no_templates", (0L, text1))) ::
      (EntityId("456"),
          slice("info:delta_no_templates", (0L, text2))) ::
      (EntityId("789"),
          slice("info:delta_no_templates", (0L, text3))) ::
      (EntityId("123"), slice("info:is_reverted", (0L, true))) ::
      (EntityId("456"), slice("info:is_reverted", (0L, true))) ::
      (EntityId("789"), slice("info:is_reverted", (0L, true))) ::
      Nil

  /**
   * Validates the top next songs produces for the three songs used in the test input. The counts
   * should be as follows.
   *
   * Played First     Played Second     Count
   * song-0           song-0            1
   * song-0           song-1            2
   * song-0           song-2            0
   * song-1           song-0            0
   * song-1           song-1            0
   * song-1           song-2            2
   * song-2           song-0            0
   * song-2           song-1            1
   * song-2           song-2            0
   *
   * @param topNextSongs contains three tuples for three songs, each containing a record of the
   *     top next songs played.
   */
  def validateTest(topNextSongs: Buffer[(EntityId, KijiSlice[AvroRecord])]) {
    val topSongForEachSong = topNextSongs
        .map { case(entityId, slice) =>
            (entityId(0).toString, slice) }
        .map { case(id, slice) => (id, slice.getFirstValue()("topSongs")) }

    topSongForEachSong.foreach {
      case ("song-0", topSongs) => {
        assert(2 === topSongs.asList.size)
        assert("song-1" === topSongs(0)("song_id").asString)
        assert(2 === topSongs(0)("count").asLong)
        assert("song-0" === topSongs(1)("song_id").asString)
        assert(1 === topSongs(1)("count").asLong)
      }
      case ("song-1", topSongs) => {
        assert(1 === topSongs.asList.size)
        assert("song-2" === topSongs(0)("song_id").asString)
        assert(2 === topSongs(0)("count").asLong)
      }
      case ("song-2", topSongs) => {
        assert(1 === topSongs.asList.size)
        assert("song-1" === topSongs(0)("song_id").asString)
        assert(1 === topSongs(0)("count").asLong)
      }
    }
  }

  test("TopNextSongs computes how often one song is played after another (local).") {
    val outputPath = "/home/lisa/src/wiki-express/src/test/resources/StopWordsSuiteOutput.txt"
    JobTest(new StopWords(_))
        .arg("revision-table", revisionURI)
        .arg("jobOutput", outputPath)
        .source(KijiInput(revisionURI)(Map(
            Column("info:delta_no_templates", all) -> 'revision,
            Column("revert_type:is_reverted", all) -> 'isReverted)),
            testInput)
        .sink(Tsv("jobOutput")) { validateTest }
        .run
        .finish
  }
}
