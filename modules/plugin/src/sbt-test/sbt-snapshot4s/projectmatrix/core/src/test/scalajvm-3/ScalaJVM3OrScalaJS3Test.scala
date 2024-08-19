package core

import weaver._
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.generated._

/** This test runs on the JVM platform for Scala 3 only.
  * There is a test with the same name, but different contents, for the ScalaJS platform.
  *
  * We verify that the JVM and ScalaJS snapshots are updated independently.
  */
object ScalaJVM3OrScalaJS3Test extends SimpleIOSuite with SnapshotExpectations {

  test("inline") {
    assertInlineSnapshot("scalajvm-3", ???)
  }

  test("file") {
    assertFileSnapshot("contents", "existing-file")
  }
}
