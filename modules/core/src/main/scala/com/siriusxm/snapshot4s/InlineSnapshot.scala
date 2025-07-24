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

object InlineSnapshot {

  def generate[A](
      found: A,
      startPosition: Int,
      endPosition: Int,
      sourceFile: String,
      config: SnapshotConfig,
      reprForA: Repr[A]
  ): Result[A] = {
    writeChangeFile(
      found,
      startPosition,
      endPosition,
      sourceFile,
      config,
      reprForA
    )
    Result.NonExistent(found)
  }

  def assert[A](
      found: A,
      snapshot: A,
      startPosition: Int,
      endPosition: Int,
      sourceFile: String,
      config: SnapshotConfig,
      reprForA: Repr[A],
      eq: SnapshotEq[A]
  ): Result[A] = {
    if (eq.eqv(found, snapshot)) {
      Result.Success(found, snapshot)
    } else {
      writeChangeFile(
        found,
        startPosition,
        endPosition,
        sourceFile,
        config,
        reprForA
      )
      Result.Failure(found, snapshot)
    }
  }

  private def writeChangeFile[A](
      found: A,
      startPosition: Int,
      endPosition: Int,
      sourceFile: String,
      config: SnapshotConfig,
      reprForA: Repr[A]
  ): Unit = {
    val sourceFilePath    = Path(sourceFile)
    val sourceFileContent = sourceFilePath.read()
    val sourceFileHash    = Hashing.calculateHash(sourceFileContent)
    val hashHeader        = Hashing.produceHashHeader(sourceFileHash)
    val changeFile        =
      config.outputDirectory / RelPath("inline-patch") / Locations.relativeSourceFilePath(
        sourceFile,
        config
      ) / RelPath(s"$startPosition-$endPosition")
    val actualFileContent = addNowarnAnnotations(reprForA.toSourceString(found))
    val actualStr         = s"${hashHeader}\n${actualFileContent}"
    changeFile.write(actualStr)
  }

  // See the Scala 2.13 compiler for the source of the warning we're ignoring:
  // https://github.com/scala/scala/blob/2.13.x/src/compiler/scala/tools/nsc/typechecker/Typers.scala#L118
  private final val InterpolatorCodeRegex  = """\$\{\s*(.*?)\s*\}""".r
  private final val InterpolatorIdentRegex = """\$[$\w]+""".r

  private[snapshot4s] def addNowarnAnnotations(str: String): String = {
    if (
      InterpolatorCodeRegex.findFirstIn(str).nonEmpty || InterpolatorIdentRegex
        .findFirstIn(str)
        .nonEmpty
    ) {
      s"""$str: @scala.annotation.nowarn("msg=possible missing interpolator")"""
    } else {
      str
    }
  }

}
