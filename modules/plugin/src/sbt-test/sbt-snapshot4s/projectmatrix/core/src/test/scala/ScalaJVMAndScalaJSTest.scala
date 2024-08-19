package core

import weaver._
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.generated._

/** This test runs on the both the JVM and JS platforms for all Scala versions. */
object ScalaJVMAndScalaJSTest extends SimpleIOSuite with SnapshotExpectations {

  test("inline") {
    assertInlineSnapshot("scalajvm-and-js", ???)
  }

  test("file") {
    assertFileSnapshot("contents", "existing-file")
  }
}
