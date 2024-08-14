package simple

import snapshot4s.munit.SnapshotAssertions
import snapshot4s.multiple_frameworks.generated._

class MunitSimpleTest extends munit.FunSuite with SnapshotAssertions {

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
