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
import sbt.util.Logger

object Snapshot4sPlugin extends AutoPlugin {

  object autoImport {
    val snapshot4sResourceDirectory =
      settingKey[File]("The directory in which snapshot4s snapshot files are stored.")
    val snapshot4sDirectory =
      settingKey[File]("The directory in which snapshot4s results are stored prior to promotion.")
    val snapshot4sSourceGenerator =
      taskKey[Seq[File]]("Generate source files for snapshot4s testing.")
    val snapshot4sPromote = inputKey[Unit]("Update failing snapshot4s snapshot files.")
  }

  import autoImport.*

  override def projectSettings: Seq[Setting[?]] = Seq(
    snapshot4sDirectory         := (Test / target).value / "snapshot",
    snapshot4sResourceDirectory := (Test / resourceDirectory).value / "snapshot",
    snapshot4sSourceGenerator := {
      val text = s"""package snapshot4s

/** This object was generated by sbt-snapshot4s. */
object generated {
  implicit val snapshotConfig: SnapshotConfig = new SnapshotConfig(
    resourceDirectory = Path("${snapshot4sResourceDirectory.value}"),
    outputDirectory = Path("${snapshot4sDirectory.value}"),
    sourceDirectory = Path("${sourceBaseDirectory((Test / sourceDirectories).value)}")
  )
}
 """
      val snapshot4s = (Test / sourceManaged).value / "Snapshot4sBuildInfo.scala"
      IO.write(snapshot4s, text)
      Seq(snapshot4s)
    },
    Test / sourceGenerators += snapshot4sSourceGenerator.taskValue,
    Test / scalacOptions ++= {
      if (scalaVersion.value.startsWith("3.")) {
        Nil
      } else {
        Seq(
          "-Yrangepos" // makes the compiler collect position metadata
        )
      }
    },
    snapshot4sPromote := {
      val log = streams.value.log
      val arguments =
        spaceDelimited("<tests filter>")
          .examples("*MySuite*", "*MySuite.scala")
          .parsed

      val filter = makeFilter(arguments)
      applyResourcePatches(log)(
        snapshot4sDirectory.value / "resource-patch",
        snapshot4sResourceDirectory.value,
        filter
      )
      applyInlinePatches(log)(
        snapshot4sDirectory.value / "inline-patch",
        sourceBaseDirectory((Test / sourceDirectories).value),
        filter
      )
    }
  )

  private def makeFilter(arguments: Seq[String]): NameFilter =
    if (arguments.isEmpty) AllPassFilter
    else arguments.map(GlobFilter(_)).reduce(_ | _)

  private def sourceBaseDirectory(sourceDirectories: Seq[File]): File = {
    def sharedParent(dirA: File, dirB: File): File = {
      if (dirB.getAbsolutePath().startsWith(dirA.getAbsolutePath())) dirA
      else if (dirA.getAbsolutePath().startsWith(dirB.getAbsolutePath())) dirB
      else sharedParent(dirA.getParentFile, dirB)
    }
    sourceDirectories.reduce(sharedParent)
  }

  private def applyResourcePatches(
      log: Logger
  )(resourcePatchDir: File, resourceDir: File, filter: NameFilter) = {
    val patches         = (resourcePatchDir ** (-DirectoryFilter)).get
    val filteredPatches = patches.filter(file => filter.accept(file.getParent))
    filteredPatches.foreach { patchFile =>
      val patchContents = IO.read(patchFile)
      val sourceFile    = locateResourceFile(resourcePatchDir, patchFile, resourceDir)
      IO.delete(patchFile)
      IO.write(sourceFile, patchContents)
      log.info(s"Patch applied to $sourceFile")
    }
  }

  private def locateResourceFile(resourcePatchDir: File, patchFile: File, resourceDir: File) = {
    val relativePath =
      IO.relativize(resourcePatchDir, patchFile).get
    // relative path starts with subpath like "src/test/scala/MyTest.scala" we need to remove that
    val withoutSourceTestFileName = relativePath.split("\\.scala/").tail.mkString("/")
    resourceDir / withoutSourceTestFileName
  }

  private def applyInlinePatches(
      log: Logger
  )(inlinePatchDir: File, sourceDir: File, filter: NameFilter) = {
    val patchDirectories = (inlinePatchDir ** (-DirectoryFilter)).get
    val dirsByParent     = patchDirectories.groupBy(_.getParent).filterKeys(filter.accept)
    dirsByParent.foreach { case (parentDir, changeFiles) =>
      val relativeSourceFile = IO.relativize(inlinePatchDir, new File(parentDir)).get
      val sourceFile         = sourceDir / relativeSourceFile
      val sourceContents     = IO.read(sourceFile)

      val sourceFileHash = Hashing.calculateHash(sourceContents)

      val changes = changeFiles.toList.mapFilter { changeFile =>
        val (start, end) = parseChangeFileName(changeFile.getName)
        val contents     = IO.read(changeFile)

        Hashing.verifyAndRemoveHash(sourceFileHash)(contents) match {
          case None =>
            log.warn(
              s"Can't apply patch to $relativeSourceFile, most likely it was modified after executing tests. " +
                s"Please re-run the test and try again."
            )
            None
          case Some(patchContent) => Some((start, end, patchContent))
        }
      }.toList

      changes match {
        case Nil => ()
        case nonEmptyList =>
          val patchedSource = applyPatch(sourceContents, nonEmptyList)
          IO.write(sourceFile, patchedSource)
          log.info(s"Patch applied to $sourceFile")
          changeFiles.foreach(IO.delete)
      }
    }

  }

  private def parseChangeFileName(str: String): (Int, Int) = {
    str.split("-").toList match {
      case startPos :: endPos :: Nil => (startPos.toInt, endPos.toInt)
      case _ =>
        sys.error(
          s"Unable to parse change file name ${str}. There is a bug in the sxm-snapshot4s-plugin."
        )
    }
  }

  private def applyPatch(source: String, patches: List[(Int, Int, String)]): String = {
    patches
      .sortBy(_._1)
      .foldLeft((source, 0))((acc, patch) => {
        val (source, offset)           = acc
        val (startPos, endPos, middle) = patch
        val start                      = source.take(offset + startPos)
        val end                        = source.drop(offset + endPos)
        val nextSource                 = start ++ middle ++ end
        val lengthIncrease             = middle.length - (endPos - startPos)
        val nextOffset                 = offset + lengthIncrease
        (nextSource, nextOffset)
      })
      ._1
  }

  private implicit class MapFilter[A](list: List[A]) {
    def mapFilter[B](f: A => Option[B]): List[B] =
      list.map(f).filter(_.isDefined).map(_.get)
  }

}
