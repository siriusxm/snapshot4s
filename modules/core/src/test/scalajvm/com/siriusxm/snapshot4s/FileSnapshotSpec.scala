package snapshot4s

import cats.effect.IO
import cats.syntax.all.*
import os.{Path as OsPath, PathChunk}
import weaver.SimpleIOSuite

import snapshot4s.*

object FileSnapshotSpec extends SimpleIOSuite {

  val comparison = SnapshotEq.instance[String]((found, expected) => {
    found === expected
  })

  val resultLike: ResultLike[String, IO[Result[String]]] =
    (f: () => Result[String]) => {
      IO(f())
    }

  private def setupConfig: IO[SnapshotConfig] = {
    val tempDir = IO.blocking(new Path(os.temp.dir()))
    (tempDir, tempDir, tempDir)
      .mapN(new SnapshotConfig(_, _, _))
  }

  private def assert(found: String, path: PathChunk)(config: SnapshotConfig) = {
    FileSnapshot(found, path.toString, config, comparison, resultLike)
  }

  private def writeSnapshot(snapshot: String, path: PathChunk)(config: SnapshotConfig) = {
    IO.blocking(os.write(config.resourceDirectory.osPath / path, snapshot))
  }

  private def getPatches(config: SnapshotConfig): IO[List[OsPath]] = {
    IO.blocking(os.walk(config.outputDirectory.osPath).filter(os.isFile).toList)
  }

  test("fail if snapshot does not exist") {
    for {
      config <- setupConfig
      result <- assert("contents", "snapshot")(config)
    } yield matches(result) { case _: Result.NonExistent[?] => success }
  }

  test("fail if snapshots are not equal") {
    for {
      config <- setupConfig
      _      <- writeSnapshot("old-contents", "snapshot")(config)
      result <- assert("new-contents", "snapshot")(config)
    } yield matches(result) { case _: Result.Failure[?] =>
      success
    }
  }

  test("succeed if snapshots are equal") {
    for {
      config <- setupConfig
      _      <- writeSnapshot("contents", "snapshot")(config)
      result <- assert("contents", "snapshot")(config)
    } yield matches(result) { case _: Result.Success[?] => success }
  }

  test("not write a patch file on success") {
    for {
      config  <- setupConfig
      _       <- writeSnapshot("contents", "snapshot")(config)
      _       <- assert("contents", "snapshot")(config)
      patches <- getPatches(config)
    } yield expect(patches.isEmpty)
  }

  test("write a patch file on failure") {
    for {
      config  <- setupConfig
      _       <- writeSnapshot("old-contents", "snapshot")(config)
      _       <- assert("new-contents", "snapshot")(config)
      patches <- getPatches(config)
    } yield expect.same(
      patches,
      List(config.outputDirectory.osPath / "resource-patch" / "snapshot")
    )
  }

  test("write a patch file in a nested directory") {
    for {
      config  <- setupConfig
      _       <- assert("new-contents", os.rel / "nested" / "snapshot")(config)
      patches <- getPatches(config)
    } yield expect.same(
      patches,
      List(config.outputDirectory.osPath / "resource-patch" / "nested" / "snapshot")
    )
  }
}
