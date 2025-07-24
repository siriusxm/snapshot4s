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

package snapshot4s.scalatest

import scala.collection.immutable.IndexedSeq

import org.scalactic.Prettifier
import org.scalactic.source.Position
import org.scalatest.exceptions.TestFailedException
import org.scalatest.{Assertion, Succeeded}

import snapshot4s.*

private object ScalaTestResultLike {

  def resultLike[A](pos: Position, prettifier: Prettifier): ResultLike[A, Assertion] =
    new ResultLike[A, Assertion] {

      def apply(result: () => Result[A]): Assertion = {
        resultToAssertion(result(), pos, prettifier)
      }
    }

  private def resultToAssertion[A](
      result: Result[A],
      pos: Position,
      prettifier: Prettifier
  ): Assertion = {
    result match {
      case _: Result.Success[?]     => Succeeded
      case _: Result.NonExistent[?] =>
        throw new TestFailedException(
          _ => Some(ErrorMessages.nonExistent),
          cause = None,
          posOrStackDepthFun = Left(pos),
          payload = None,
          analysis = IndexedSeq.empty[String]
        )
      case Result.Failure(found, snapshot) =>
        val diff = prettifier(found, snapshot)
        throw new TestFailedException(
          _ => Some(s"${ErrorMessages.failure} Expected: ${diff.right}, but got ${diff.left}"),
          cause = None,
          posOrStackDepthFun = Left(pos),
          payload = None,
          analysis = IndexedSeq.empty[String]
        )
    }
  }
}
