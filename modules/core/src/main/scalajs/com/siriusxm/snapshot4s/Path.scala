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

package snapshot4s

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

import org.typelevel.scalaccompat.annotation.*

final class Path private (override val toString: String) extends PathApi {
  private[snapshot4s] def read(): String = facade.read(toString, "utf8")

  private[snapshot4s] def write(contents: String): Unit = {
    facade.mkdir(
      facade.dirname(toString),
      new facade.MkdirOptions {
        recursive = true
      }
    )
    facade.write(
      toString,
      contents,
      new facade.WriteFileOptions {
        encoding = "utf8"
        flag = "w+"
        flush = true
      }
    )
  }

  private[snapshot4s] def relativeTo(baseDirectory: Path): RelPath =
    RelPath(facade.relative(baseDirectory.toString, toString))

  private[snapshot4s] def /(path: RelPath): Path =
    Path(facade.join(toString, path.toString))

  private[snapshot4s] def exists(): Boolean =
    facade.exists(toString)
}

object Path extends PathCompanionApi {
  def apply(path: String): Path = new Path(path)
}

@nowarn212("cat=unused")
private object facade {

  trait MkdirOptions extends js.Object {
    var recursive: js.UndefOr[Boolean] = js.undefined
  }

  trait WriteFileOptions extends js.Object {
    var encoding: js.UndefOr[String] = js.undefined
    var mode: js.UndefOr[String]     = js.undefined
    var flag: js.UndefOr[String]     = js.undefined
    var flush: js.UndefOr[Boolean]   = js.undefined
  }

  @js.native
  @JSImport("path", "dirname")
  def dirname(path: String): String = js.native

  @js.native
  @JSImport("path", "join")
  def join(paths: String*): String = js.native

  @js.native
  @JSImport("path", "relative")
  def relative(from: String, to: String): String = js.native

  @js.native
  @JSImport("fs", "readFileSync")
  def read(file: String, encoding: String): String = js.native

  @js.native
  @JSImport("fs", "mkdirSync")
  def mkdir(file: String, options: MkdirOptions): Unit = js.native

  @js.native
  @JSImport("fs", "writeFileSync")
  def write(file: String, data: String, options: WriteFileOptions): Unit = js.native

  @js.native
  @JSImport("fs", "existsSync")
  def exists(file: String): Boolean = js.native
}
