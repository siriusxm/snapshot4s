package core

import weaver.*
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.generated.*

/** This test runs on the both the JVM and JS platforms for Scala 3 only.
  * There is a test with the same name for Scala 2.
  *
  * We verify that the Scala 2 and Scala 3 snapshots are updated independently.
  */
object ScalaJVMAndScalaJS2Or3Test extends SimpleIOSuite with SnapshotExpectations:

  test("inline") {
    assertInlineSnapshot("scalajvm-3-and-js-3", ???)
  }

  test("file") {
    assertFileSnapshot("contents", "existing-file")
  }
