/*
 * Copyright 2024 SiriusXM
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

package snapshot4s

private[snapshot4s] object Hashing {

  private val sourceFileHashPrefix = "# Hash:"

  def extractHash(input: String): Option[String] =
    input.linesIterator.toList.headOption
      .filter(_.startsWith(sourceFileHashPrefix))
      .map(_.replaceAll(sourceFileHashPrefix, "").trim())

  /** Verifies if the patch contains expected hash.
    * If the hash matches, returns patch without hash header or None otherwise.
    *
    * @param expectedHash expected value of hash to be found in patch file
    * @param patchContent contents of patch file
    * @return
    */
  def verifyAndRemoveHash(expectedHash: String)(patchContent: String): Option[String] = {
    patchContent.linesIterator.toList match {
      case head :: tail if (extractHash(head).contains(expectedHash)) =>
        Some(tail.mkString("\n"))
      case _ => None
    }
  }

  def produceHashHeader(hash: String): String =
    s"$sourceFileHashPrefix $hash"

  def calculateHash(input: String): String =
    input.hashCode().toHexString

}
