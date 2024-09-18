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

import scala.reflect.macros.blackbox

private[snapshot4s] trait AssertFileSnapshotMacro[R] {

  /** Assert that a found value is equal to a previously snapshotted value.
    *
    * If the assertion fails, the file can be recreated with the found contents using `snapshot4sPromote`.
    * @see https://siriusxm.github.io/snapshot4s/file-snapshots
    *
    * @param found        The found value.
    * @param snapshotPath Path to file containing the previously snapshotted value. This is relative to the resources/snapshot directory.
    * @param eq           Compares the found and snapshot values.
    * @param resultLike   Constructs a framework-specific result.
    */
  def assertFileSnapshot(found: String, snapshotPath: String)(implicit
      config: SnapshotConfig,
      snapshotEq: SnapshotEq[String],
      resultLike: ResultLike[String, R]
  ): R = macro AssertFileSnapshotMacro.Macro.impl[R]
}

private[snapshot4s] object AssertFileSnapshotMacro {

  class Macro(val c: blackbox.Context) {
    import c.universe.*

    def impl[E](
        found: Expr[String],
        snapshotPath: Expr[String]
    )(
        config: Expr[SnapshotConfig],
        snapshotEq: Expr[SnapshotEq[String]],
        resultLike: Expr[ResultLike[String, E]]
    ): Tree = {
      // Scala 2 macro system will place this call in client code so the called method must be public
      // `FileSnapshotProxy.createFileSnapshot` is introduced to keep `FileSnapshot` private
      q"""_root_.snapshot4s.FileSnapshotProxy.createFileSnapshot($found, $snapshotPath, ${c.enclosingPosition.source.path}, $config, $snapshotEq, $resultLike)"""
    }
  }

}

object FileSnapshotProxy {

  def createFileSnapshot[E](
      found: String,
      snapshotPath: String,
      sourceFile: String,
      config: SnapshotConfig,
      snapshotEq: SnapshotEq[String],
      resultLike: ResultLike[String, E]
  ): E = FileSnapshot(found, snapshotPath, sourceFile, config, snapshotEq, resultLike)
}
