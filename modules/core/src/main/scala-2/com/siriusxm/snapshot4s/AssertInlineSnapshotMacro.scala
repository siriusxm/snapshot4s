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

private[snapshot4s] trait AssertInlineSnapshotMacro[R] {

  /** Assert that a found value is equal to an expected value.
    *
    * If the assertion fails, the expected value can be replaced with the found value using `snapshot4sPromote`.
    * @see https://siriusxm.github.io/snapshot4s/inline-snapshots
    *
    * @param found           The found value.
    * @param snapshot        The expected value.
    * @param reprForA        Determines the source representation of `A`.
    * @param eqForA          Compares the found and snapshot values.
    * @param resultLikeForA  Constructs a framework-specific result.
    */
  def assertInlineSnapshot[A](found: A, snapshot: A)(implicit
      config: SnapshotConfig,
      reprForA: Repr[A],
      eqForA: SnapshotEq[A],
      resultLikeForA: ResultLike[A, R]
  ): R = macro AssertInlineSnapshotMacro.Macro.impl[A, R]
}

private[snapshot4s] object AssertInlineSnapshotMacro {

  class Macro(val c: blackbox.Context) {
    import c.universe.*

    def impl[A, E](
        found: Expr[A],
        snapshot: Expr[A]
    )(
        config: Expr[SnapshotConfig],
        reprForA: Expr[Repr[A]],
        eqForA: Expr[SnapshotEq[A]],
        resultLikeForA: Expr[ResultLike[A, E]]
    ): Tree = {
      val result = snapshot.tree match {
        case q"scala.Predef.???" =>
          q"""_root_.snapshot4s.InlineSnapshot.generate($found, ${snapshot.tree.pos.start},${snapshot.tree.pos.end},${c.enclosingPosition.source.path}, $config, $reprForA)"""
        case _ =>
          q"""_root_.snapshot4s.InlineSnapshot.assert($found, $snapshot, ${snapshot.tree.pos.start},${snapshot.tree.pos.end},${c.enclosingPosition.source.path}, $config, $reprForA, $eqForA)"""
      }
      q"""$resultLikeForA.apply(() => $result)"""
    }
  }
}
