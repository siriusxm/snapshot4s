package extra

import weaver._
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.generated._

object ExtraTest extends SimpleIOSuite with SnapshotExpectations {

  test("inline") {
    assertInlineSnapshot(1, 2)
  }

  test("new inline snapshot") {
    assertInlineSnapshot(1, ???)
  }

  test("file that doesn't exist") {
    assertFileSnapshot("extra-contents", "nonexistent-file")
  }

  test("existing file") {
    assertFileSnapshot("extra-contents", "existing-file")
  }
}
