package snapshot4s

import cats.effect.IO
import weaver.*

object InlineSnapshotSpec extends SimpleIOSuite {

  pureTest("calculates relative paths correctly") {
    val config = new SnapshotConfig(
      resourceDirectory = Path("/path/to/resources"),
      outputDirectory = Path("/path/to/output"),
      sourceDirectory = Path("/path/to/sources")
    )
    val relativePath =
      InlineSnapshot.relativeSourceFilePath("/path/to/sources/TestFile.scala", config)
    expect.eql(relativePath.value, "TestFile.scala")
  }

  test("raises errors if the source directory is calculated incorrectly") {
    val config = new SnapshotConfig(
      resourceDirectory = Path("/path/to/resources"),
      outputDirectory = Path("/path/to/output"),
      sourceDirectory = Path("/wrong/path/to/sources")
    )
    val result =
      IO(InlineSnapshot.relativeSourceFilePath("/path/to/sources/TestFile.scala", config))
    val message =
      """Your project setup is not supported by snapshot4s. We encourage you to raise an issue at https://github.com/siriusxm/snapshot4s.

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
