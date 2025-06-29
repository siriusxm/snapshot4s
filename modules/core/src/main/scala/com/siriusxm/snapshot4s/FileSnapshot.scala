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

private[snapshot4s] object FileSnapshot {

  def apply[E](
      found: String,
      snapshotPath: String,
      sourceFile: String,
      config: SnapshotConfig,
      eq: SnapshotEq[String],
      resultLike: ResultLike[String, E]
  ): E = resultLike { () =>
    val relativePath         = Locations.relativeSourceFilePath(sourceFile, config)
    val absoluteSnapshotPath = config.resourceDirectory / RelPath(snapshotPath)
    def writePatchFile()     = {
      val patchPath =
        config.outputDirectory / RelPath("resource-patch") / relativePath / RelPath(snapshotPath)
      patchPath.write(found)
    }
    if (absoluteSnapshotPath.exists()) {
      val snapshot = absoluteSnapshotPath.read()
      if (eq.eqv(found, snapshot)) {
        Result.Success(found, snapshot)
      } else {
        writePatchFile()
        Result.Failure(found, snapshot)
      }
    } else {
      writePatchFile()
      Result.NonExistent(found)
    }
  }

}
