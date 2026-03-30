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

import sbt.*
import sbt.Keys.*
import sbt.complete.DefaultParsers.*

object Snapshot4sPlugin extends AutoPlugin {

  object autoImport {
    val snapshot4sResourceDirectory =
      settingKey[File]("The directory in which snapshot4s snapshot files are stored.")
    val snapshot4sDirectory =
      settingKey[File]("The directory in which snapshot4s results are stored prior to promotion.")

    // Opt out of sbt 2 task caching: this task writes a managed source file as a side effect.
    // If cached, sbt may reuse the returned Seq[File] without recreating the file after cleanup.
    @transient
    val snapshot4sSourceGenerator =
      taskKey[Seq[File]]("Generate source files for snapshot4s testing.")
    val snapshot4sPromote = inputKey[Unit]("Update failing snapshot4s snapshot files.")
  }

  import autoImport.*

  override def projectSettings: Seq[Setting[?]] = Seq(
    snapshot4sDirectory         := (Test / target).value / "snapshot",
    snapshot4sResourceDirectory := Plugin.defaultResourceDirectory(
      (Test / resourceDirectory).value
    ),
    snapshot4sSourceGenerator := {
      val sourceFile = Plugin.sourceGenerator(
        fileIO
      )(
        resourceDir = snapshot4sResourceDirectory.value,
        patchDir = snapshot4sDirectory.value,
        sourceDirs = (Test / sourceDirectories).value,
        destDir = (Test / sourceManaged).value
      )
      Seq(sourceFile)
    },
    Test / sourceGenerators += snapshot4sSourceGenerator.taskValue,
    Test / scalacOptions ++= Plugin.scalacOptions(scalaVersion.value),
    snapshot4sPromote := {
      val log       = streams.value.log
      val arguments =
        spaceDelimited("<tests filter>")
          .examples("*MySuite*", "*MySuite.scala")
          .parsed

      Plugin.applyResourcePatches(logInfo = log.info(_), fileIO = fileIO)(
        patchDir = snapshot4sDirectory.value,
        resourceDir = snapshot4sResourceDirectory.value,
        arguments = arguments
      )
      Plugin.applyInlinePatches(logInfo = log.info(_), logWarn = log.warn(_), fileIO = fileIO)(
        patchDir = snapshot4sDirectory.value,
        sourceDirs = (Test / sourceDirectories).value,
        arguments = arguments
      )
    }
  )

  private val fileIO: FileIO = new FileIO {
    def read(file: File): String                   = IO.read(file)
    def write(file: File, contents: String): Unit  = IO.write(file, contents)
    def delete(file: File): Unit                   = IO.delete(file)
    def relativize(base: File, file: File): String = IO.relativize(base, file).get

    def listMatchingFiles(base: File, matcher: File => Boolean): Seq[File] = {
      val filter = new FileFilter {
        def accept(file: File): Boolean = matcher(file)
      }
      (base ** filter).get()
    }
  }

}
