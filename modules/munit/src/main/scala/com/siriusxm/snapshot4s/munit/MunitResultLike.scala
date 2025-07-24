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

package snapshot4s.munit

import _root_.munit.diff.DiffOptions
import _root_.munit.{Assertions, Location}

import snapshot4s.*

private object MunitResultLike {

  def resultLike[A](loc: Location, diffOptions: DiffOptions): ResultLike[A, Unit] =
    new ResultLike[A, Unit] {

      def apply(result: () => Result[A]): Unit = {
        resultToAssertion(result(), loc, diffOptions)
      }
    }

  private def resultToAssertion[A](
      result: Result[A],
      loc: Location,
      diffOptions: DiffOptions
  ): Unit = {
    result match {
      case _: Result.Success[?]     => ()
      case _: Result.NonExistent[?] =>
        throw Assertions.fail(ErrorMessages.nonExistent)(loc, diffOptions)
      case Result.Failure(found, snapshot) =>
        throw Assertions
          .fail(diffReport(found.toString, snapshot.toString, diffOptions))(loc, diffOptions)
    }
  }

  private def diffReport(found: String, expected: String, diffOptions: DiffOptions): String = {
    import _root_.munit.diff.Diff
    val diff = new Diff(
      obtained = found,
      expected = expected,
      options = diffOptions
    )
    diff.createReport(ErrorMessages.failure, printObtainedAsStripMargin = false)
  }
}
