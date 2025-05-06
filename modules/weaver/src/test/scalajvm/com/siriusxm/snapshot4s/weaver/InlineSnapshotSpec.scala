/*
 * Copyright 2024 SiriusXM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package snapshot4s.weaver

import cats.effect.IO
import cats.syntax.all.*
import os.Path as OsPath
import weaver.*

import snapshot4s.*

object InlineSnapshotSpec extends SimpleIOSuite with SnapshotExpectations {

  import SnapshotExpectations.assertInlineSnapshot as assertSnapshot

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

  test("fail if snapshots are not equal") {
    for {
      config <- setupConfig
      result <- assertSnapshot("new-contents", "old-contents")(
        using
        config,
        implicitly,
        implicitly,
        implicitly
      )
    } yield expect(result.run.isInvalid)
  }

  test("succeed if snapshots are equal") {
    for {
      config <- setupConfig
      result <- assertSnapshot("contents", "contents")(
        using
        config,
        implicitly,
        implicitly,
        implicitly
      )
    } yield expect(result.run.isValid)
  }

  test("not write a patch file on success") {
    for {
      config <- setupConfig
      _ <- assertSnapshot("contents", "contents")(using config, implicitly, implicitly, implicitly)
      patches <- getPatches(config)
    } yield expect(patches.isEmpty)
  }

  test("write a patch file on failure") {
    for {
      config <- setupConfig
      _ <- assertSnapshot("new-contents", "old-contents")(
        using
        config,
        implicitly,
        implicitly,
        implicitly
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
      _ <- assertSnapshot("new-contents", ???)(
        using
        config,
        implicitly,
        implicitly,
        implicitly
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
      _ <- assertSnapshot("new-contents", "old-contents")(
        using
        config,
        implicitly,
        implicitly,
        implicitly
      )
      _ <- assertSnapshot("other-contents", "old-contents")(
        using
        config,
        implicitly,
        implicitly,
        implicitly
      )
      patches <- getPatches(config)
    } yield expect.eql(patches.length, 2) && forEach(patches)(path =>
      expect(
        path.startsWith(config.outputDirectory.osPath / "inline-patch" / "InlineSnapshotSpec.scala")
      )
    )
  }
}
