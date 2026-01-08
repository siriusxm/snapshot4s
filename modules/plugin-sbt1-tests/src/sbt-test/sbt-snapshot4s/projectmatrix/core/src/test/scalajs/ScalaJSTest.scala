package core

import weaver.*
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.generated.*

/** This test runs on the JS platform for all Scala versions. */
object ScalaJSTest extends SimpleIOSuite with SnapshotExpectations {

  test("inline") {
    assertInlineSnapshot("scalajs", ???)
  }

  test("file") {
    assertFileSnapshot("contents", "existing-file")
  }
}
