package simple

import org.scalatest.flatspec.AnyFlatSpec
import snapshot4s.scalatest.SnapshotAssertions
import snapshot4s.generated._

class SimpleTest extends AnyFlatSpec with SnapshotAssertions {

  "snapshot4s" should "assert inline on existing values" in {
    assertInlineSnapshot(1, 2)
  }

  it should "assert inline on unimplemented values" in {
    assertInlineSnapshot(1, ???)
  }

  it should "assert on a file that doesn't exist" in {
    assertFileSnapshot("contents", "nonexistent-file")
  }

  it should "assert on an existing file" in {
    assertFileSnapshot("contents", "existing-file")
  }

  it should "assert on a file in a nested directory" in {
    assertFileSnapshot("contents", "nested-directory/nested-file")
  }
}
