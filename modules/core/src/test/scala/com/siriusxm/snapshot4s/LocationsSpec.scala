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

import cats.effect.IO
import cats.effect.std.Env
import weaver.*

object LocationsSpec extends SimpleIOSuite {

  pureTest("calculates relative paths correctly") {
    val config = new SnapshotConfig(
      resourceDirectory = Path("/path/to/resources"),
      outputDirectory = Path("/path/to/output"),
      sourceDirectory = Path("/path/to/sources")
    )
    val relativePath =
      Locations.relativeSourceFilePath("/path/to/sources/TestFile.scala", config)
    expect.eql(relativePath.value, "TestFile.scala")
  }

  test("raises errors if the source directory is calculated incorrectly") {
    def makePaths(root: String, sep: String): (SnapshotConfig, String, String) = {
      val config = new SnapshotConfig(
        resourceDirectory = Path(s"${root}${sep}path${sep}to${sep}resources"),
        outputDirectory = Path(s"${root}${sep}path${sep}to${sep}output"),
        sourceDirectory = Path(s"${root}${sep}wrong${sep}path${sep}to${sep}sources")
      )
      val relativeSourcePath = s"${root}${sep}path${sep}to${sep}sources${sep}TestFile.scala"
      val message            =
        s"""Your project setup is not supported by snapshot4s. We encourage you to raise an issue at https://github.com/siriusxm/snapshot4s/issues/new?template=bug.md

We have detected the following configuration:
  - sourceDirectory: ${root}${sep}wrong${sep}path${sep}to${sep}sources
  - resourceDirectory: ${root}${sep}path${sep}to${sep}resources
  - outputDirectory: ${root}${sep}path${sep}to${sep}output
"""
      (config, relativeSourcePath, message)
    }
    for {
      isRunningOnWindows <- Env[IO]
        .get("RUNNER_OS")
        .map(_.exists(_.toLowerCase.contains("windows")))
      (config, path, message) =
        // Using java.io.File.separator results in linking errors in JS, so hard-code file separators instead.
        if (isRunningOnWindows) makePaths("D:", "\\")
        else makePaths("", "/")
      result <- IO(Locations.relativeSourceFilePath(path, config)).attempt
    } yield matches(result) { case Left(err) => expect.eql(message, err.getMessage) }
  }
}
