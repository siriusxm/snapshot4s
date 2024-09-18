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
    val config = new SnapshotConfig(
      resourceDirectory = Path("/path/to/resources"),
      outputDirectory = Path("/path/to/output"),
      sourceDirectory = Path("/wrong/path/to/sources")
    )
    val result =
      IO(Locations.relativeSourceFilePath("/path/to/sources/TestFile.scala", config))
    val message =
      """Your project setup is not supported by snapshot4s. We encourage you to raise an issue at https://github.com/siriusxm/snapshot4s/issues/new?template=bug.md

We have detected the following configuration:
  - sourceDirectory: /wrong/path/to/sources
  - resourceDirectory: /path/to/resources
  - outputDirectory: /path/to/output
"""
    result.attempt.map { result =>
      matches(result) { case Left(err) =>
        expect.eql(err.getMessage, message)
      }
    }

  }

}
