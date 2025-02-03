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

import _root_.weaver.{AssertionException, Expectations, SourceLocation}
import cats.data.{NonEmptyList, Validated}
import cats.effect.IO

import snapshot4s.{ErrorMessages, Result, ResultLike}

object WeaverResultLike {

  def resultLike[A](loc: SourceLocation): ResultLike[A, IO[Expectations]] =
    new ResultLike[A, IO[Expectations]] {
      type Assertion = IO[Expectations]
      def apply(result: () => Result[A]): Assertion =
        IO(result()).map(resultToExpectations(_, loc))
    }

  def resultToExpectations[A](
      result: Result[A],
      loc: SourceLocation
  ): Expectations = {
    result match {
      case _: Result.Success[?] => Expectations.Helpers.success
      case _: Result.NonExistent[?] =>
        Expectations(
          Validated.invalidNel[AssertionException, Unit](
            AssertionException(ErrorMessages.nonExistent, NonEmptyList.of(loc))
          )
        )
      case Result.Failure(found, snapshot) =>
        Expectations(
          Validated.invalidNel[AssertionException, Unit](
            AssertionException(Diff(found.toString, snapshot.toString), NonEmptyList.of(loc))
          )
        )
    }
  }
}
