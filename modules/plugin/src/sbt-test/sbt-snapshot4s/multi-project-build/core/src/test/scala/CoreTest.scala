package core

import weaver._
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.generated._

object CoreTest extends SimpleIOSuite with SnapshotExpectations {

  test("inline") {
    assertInlineSnapshot(1, 2)
  }

  test("new inline snapshot") {
    assertInlineSnapshot(1, ???)
  }

  test("file that doesn't exist") {
    assertFileSnapshot("core-contents", "nonexistent-file")
  }

  test("existing file") {
    assertFileSnapshot("core-contents", "existing-file")
  }
}
