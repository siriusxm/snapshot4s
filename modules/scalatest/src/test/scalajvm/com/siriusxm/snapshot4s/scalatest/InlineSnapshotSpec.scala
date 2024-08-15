package snapshot4s.scalatest

import cats.effect.IO
import cats.syntax.all.*
import org.scalatest.{Assertion, Succeeded}
import os.Path as OsPath
import weaver.*

object InlineSnapshotSpec extends SimpleIOSuite with SnapshotAssertions {

  import snapshot4s.*

  import SnapshotAssertions.{assertInlineSnapshot as assertSnapshot}

  private def setupConfig: IO[SnapshotConfig] = {
    val tempDir = IO(new Path(os.temp.dir()))
    (
      tempDir,
      tempDir,
      IO(new Path(OsPath(implicitly[SourceLocation].filePath) / os.up))
    )
      .mapN(new SnapshotConfig(_, _, _))
  }

  private def getPatches(config: SnapshotConfig): IO[List[OsPath]] = {
    IO.blocking(os.walk(config.outputDirectory.osPath).filter(os.isFile).toList)
  }

  private def suspend(result: => Assertion): IO[Either[Throwable, Assertion]] =
    IO.blocking(result).attempt

  test("fail if snapshots are not equal") {
    for {
      config <- setupConfig
      result <- suspend(
        assertSnapshot("new-contents", "old-contents")(
          using config,
          implicitly,
          implicitly,
          implicitly
        )
      )
    } yield expect(result.isLeft)
  }

  test("succeed if snapshots are equal") {
    for {
      config <- setupConfig
      result <- suspend(
        assertSnapshot("contents", "contents")(
          using config,
          implicitly,
          implicitly,
          implicitly
        )
      )
    } yield expect(result == Right(Succeeded))
  }

  test("not write a patch file on success") {
    for {
      config <- setupConfig
      _ <- suspend(
        assertSnapshot("contents", "contents")(using config, implicitly, implicitly, implicitly)
      )
      patches <- getPatches(config)
    } yield expect(patches.isEmpty)
  }

  test("write a patch file on failure") {
    for {
      config <- setupConfig
      _ <- suspend(
        assertSnapshot("new-contents", "old-contents")(
          using config,
          implicitly,
          implicitly,
          implicitly
        )
      )
      patches <- getPatches(config)
    } yield matches(patches) { case List(patch) =>
      expect(
        patch.startsWith(
          config.outputDirectory.osPath / "inline-patch" / "InlineSnapshotSpec.scala"
        )
      )
    }
  }

  test("generate a patch on ???") {
    for {
      config <- setupConfig
      _ <- suspend(
        assertSnapshot("new-contents", ???)(
          using config,
          implicitly,
          implicitly,
          implicitly
        )
      ): @scala.annotation.nowarn(
        "msg=dead code following this construct"
      )
      patches <- getPatches(config)
    } yield matches(patches) { case List(patch) =>
      expect(
        patch.startsWith(
          config.outputDirectory.osPath / "inline-patch" / "InlineSnapshotSpec.scala"
        )
      )
    }
  }

  test("write a patch file for each assertion") {
    for {
      config <- setupConfig
      _ <- suspend(
        assertSnapshot("new-contents", "old-contents")(
          using config,
          implicitly,
          implicitly,
          implicitly
        )
      )
      _ <- suspend(
        assertSnapshot("other-contents", "old-contents")(
          using config,
          implicitly,
          implicitly,
          implicitly
        )
      )
      patches <- getPatches(config)
    } yield expect.eql(patches.length, 2) && forEach(patches)(path =>
      expect(
        path.startsWith(config.outputDirectory.osPath / "inline-patch" / "InlineSnapshotSpec.scala")
      )
    )
  }
}
