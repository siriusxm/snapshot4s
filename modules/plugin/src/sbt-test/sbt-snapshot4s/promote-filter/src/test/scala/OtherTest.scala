package simple

import weaver.*
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.generated.snapshotConfig

object OtherTest extends SimpleIOSuite with SnapshotExpectations {

  test("inline") {
    assertInlineSnapshot(123, 456)
  }

  test("new inline snapshot") {
    assertInlineSnapshot(1, ???)
  }

  test("file that doesn't exist") {
    assertFileSnapshot("other-contents", "other-nonexistent-file")
  }

  test("existing file") {
    assertFileSnapshot("contents", "existing-file")
  }

  test("file in nested directory") {
    assertFileSnapshot("contents", "nested-directory/nested-file")
  }

}
