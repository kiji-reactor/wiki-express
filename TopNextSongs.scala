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

package org.kiji.express.music

import scala.collection.JavaConverters._

import com.twitter.scalding._

import com.google.common.collect.Lists
import org.kiji.express._
import org.kiji.express.KijiJob
import org.kiji.express.DSL._

/**
 * For each song S, create a list of songs sorted by the number of times a song was played after
 * S.
 *
 * This job accepts two command line arguments, `--users-table` and `--songs-table` that should be
 * set to the Kiji URIs of a users and songs table in Kiji. The play histories of users (stored
 * in column `info:track_plays`) are used to compute how many times each song is played after
 * another. The top next songs for each song are written to the column `info:top_next_songs` of
 * the songs table.
 *
 * @param args passed from the command line.
 */
class TopNextSongs(args: Args) extends KijiJob(args) {
  /**
   * Transforms a Scala `List` into a Java `List`.
   *
   * @param ls is the list to transform.
   * @tparam T is the type of data in the list.
   * @return a Java `List` created from the original Scala `List`.
   */
  def scalaListToJavaList[T](ls: List[T]): java.util.List[T] = Lists.newArrayList[T](ls.asJava)

  /**
   * Transforms a slice of song ids into a collection of tuples `(s1,
   * s2)` signifying that `s2` appeared after `s1` in the slice, chronologically.
   *
   * @param slice of song ids representing a user's play history.
   * @return a list of song bigrams.
   */
  def bigrams(slice: KijiSlice[String]): List[(String, String)] = {
    slice.orderChronologically().cells.sliding(2)
        .map { itr => itr.iterator }
        .map { itr => (itr.next().datum, itr.next().datum) }
        .toList
  }

  /**
   * Transforms a group of tuples into a group containing a list of song count records,
   * sorted by count.
   *
   * @param nextSongs is the group of tuples containing song count records.
   * @return a group containing a list of song count records, sorted by count.
   */
  def sortNextSongs(nextSongs: GroupBuilder): GroupBuilder = {
    nextSongs.sortBy('count).reverse.toList[AvroRecord]('songCount -> 'scalaTopSongs)
  }

  // This Scalding pipeline does the following:
  // 1. Reads the column "info:track_plays" from a users table in Kiji.
  // 2. Transforms each user's play history into a collection of bigrams that record when one song
  //    was played after another.
  // 3. Counts the number of times each song was played after another.
  // 4. Creates a song count Avro record from each bigram.
  // 5. For each song S, creates a list of songs sorted by the number of times the song was played
  //    after S.
  // 6. Converts that list from a Scala list to a Java list.
  // 7. Packs each list into an Avro record.
  // 8. Creates an entity id for the songs table for each song.
  // 9. Writes each song's TopSongs record to Kiji.
  KijiInput(args("users-table"))(Map(Column("info:track_plays", all) -> 'playlist))
      .flatMap('playlist -> ('firstSong, 'songId)) { bigrams }
      .groupBy(('firstSong, 'songId)) { _.size('count) }
      .packAvro(('songId, 'count) -> 'songCount)
      .groupBy('firstSong) { sortNextSongs }
      .map('scalaTopSongs -> 'topSongs) { scalaListToJavaList }
      .packAvro('topSongs -> 'topNextSongs)
      .map('firstSong -> 'entityId) { firstSong: String =>
          EntityId(args("songs-table"))(firstSong) }
      .write(KijiOutput(args("songs-table"))('topNextSongs -> "info:top_next_songs"))
}
