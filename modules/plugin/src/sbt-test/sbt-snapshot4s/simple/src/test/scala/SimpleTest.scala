package simple

import weaver._
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.simple.generated.snapshotConfig

object SimpleTest extends SimpleIOSuite with SnapshotExpectations {

  test("inline") {
    assertInlineSnapshot(1, 2)
  }

  test("new inline snapshot") {
    assertInlineSnapshot(1, ???)
  }

  test("file that doesn't exist") {
    assertFileSnapshot("contents", "nonexistent-file")
  }

  test("existing file") {
    assertFileSnapshot("contents", "existing-file")
  }

  test("file in nested directory") {
    assertFileSnapshot("contents", "nested-directory/nested-file")
  }

}
