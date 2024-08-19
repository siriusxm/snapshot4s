package utils

import weaver._
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.generated._

object UtilsTest extends SimpleIOSuite with SnapshotExpectations {

  test("inline") {
    assertInlineSnapshot(1, 2)
  }

  test("new inline snapshot") {
    assertInlineSnapshot(1, ???)
  }

  test("file that doesn't exist") {
    assertFileSnapshot("utils-contents", "nonexistent-file")
  }

  test("existing file") {
    assertFileSnapshot("utils-contents", "existing-file")
  }
}
