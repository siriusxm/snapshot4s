package framework

import weaver.*
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.generated.*

object FrameworkTest extends SimpleIOSuite with SnapshotExpectations {

  test("inline") {
    assertInlineSnapshot(1, 2)
  }

  test("new inline snapshot") {
    assertInlineSnapshot(1, ???)
  }

  test("file that doesn't exist") {
    assertFileSnapshot("framework-contents", "nonexistent-file")
  }

  test("existing file") {
    assertFileSnapshot("framework-contents", "existing-file")
  }
}
