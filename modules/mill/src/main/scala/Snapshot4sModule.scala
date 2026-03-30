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

import java.io.File

import mill.*

/** Enables snapshot testing with snapshot4s */
trait Snapshot4sModule extends mill.scalalib.ScalaModule {

  import Snapshot4sModule.fileIO

  /** The directory in which snapshot4s snapshot files are stored. */
  def snapshot4sResourceDirectory: T[PathRef] = Task {
    val resources = super.resources().headOption.fold(moduleDir / "resources")(_.path).toNIO.toFile
    val dir       = Plugin.defaultResourceDirectory(resources)
    PathRef(os.Path(dir))
  }

  /** The directory in which snapshot4s results are stored prior to promotion. */
  def snapshot4sDirectory: T[PathRef] = Task {
    PathRef(Task.dest)
  }

  override def scalacOptions: T[Seq[String]] = Task {
    super.scalacOptions() ++ Plugin.scalacOptions(scalaVersion())
  }

  /** Generate source files for snapshot4s testing. */
  def snapshot4sSourceGenerator: T[PathRef] = Task {
    val sourceFile = Plugin.sourceGenerator(fileIO)(
      resourceDir = snapshot4sResourceDirectory().path.toNIO.toFile,
      patchDir = snapshot4sDirectory().path.toNIO.toFile,
      sourceDirs = super.sources().map(_.path.toNIO.toFile),
      destDir = Task.dest.toNIO.toFile
    )
    PathRef(os.Path(sourceFile))
  }

  override def sources: T[Seq[PathRef]] =
    super.sources() :+ snapshot4sSourceGenerator()

  /** Update failing snapshot4s snapshot files. */
  def snapshot4sPromote(args: String*): Task.Command[Unit] = Task.Command {
    Plugin.applyResourcePatches(logInfo = println(_), fileIO = fileIO)(
      patchDir = snapshot4sDirectory().path.toNIO.toFile,
      resourceDir = snapshot4sResourceDirectory().path.toNIO.toFile,
      arguments = args
    )
    Plugin.applyInlinePatches(logInfo = println(_), logWarn = println(_), fileIO = fileIO)(
      patchDir = snapshot4sDirectory().path.toNIO.toFile,
      sourceDirs = super.sources().map(_.path.toNIO.toFile),
      arguments = args
    )
  }
}

object Snapshot4sModule {

  private val fileIO: FileIO = new FileIO {
    def read(file: File): String                  = os.read(os.Path(file))
    def write(file: File, contents: String): Unit =
      os.write.over(os.Path(file), contents, createFolders = true)
    def delete(file: File): Unit = os.remove.all(os.Path(file))

    def relativize(base: File, file: File): String = {
      val relPath = os.Path(file).relativeTo(os.Path(base))
      relPath.toString
    }

    def listMatchingFiles(base: File, matcher: File => Boolean): Seq[File] = {
      val basePath = os.Path(base)
      if (os.exists(basePath))
        os.walk(basePath).map(_.toIO).filter(matcher)
      else Nil
    }
  }

}
