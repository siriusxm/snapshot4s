package core

import weaver._
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.generated._

/** This test runs on the both the JVM and JS platforms for Scala 2 only.
  * There is a test with the same name for Scala 3.
  * 
  * We verify that the Scala 2 and Scala 3 snapshots are updated independently.
  */
object ScalaJVMAndScalaJS2Or3Test extends SimpleIOSuite with SnapshotExpectations {

  test("inline") {
    assertInlineSnapshot("scalajvm-2-and-js-2", ???)
  }

  test("file") {
    assertFileSnapshot("contents", "existing-file")
  }
}
