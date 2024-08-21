package simple

import weaver.*
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.generated.snapshotConfig

object SimpleTest extends SimpleIOSuite with SnapshotExpectations {

  test("inline") {
    assertInlineSnapshot(1, 2)
  }

  test("file") {
    assertFileSnapshot("contents", "file")
  }
}
