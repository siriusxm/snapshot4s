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

import _root_.munit.{Assertions, Location}

import snapshot4s.*

private object MunitResultLike {

  def resultLike[A](loc: Location): ResultLike[A, Unit] = new ResultLike[A, Unit] {

    def apply(result: () => Result[A]): Unit = {
      resultToAssertion(result(), loc)
    }
  }

  private def resultToAssertion[A](result: Result[A], loc: Location): Unit = {
    result match {
      case _: Result.Success[?]     => ()
      case _: Result.NonExistent[?] => throw Assertions.fail("Snapshot does not exist.")(loc)
      case Result.Failure(found, snapshot) =>
        throw Assertions.fail(diffReport(found.toString, snapshot.toString))(loc)
    }
  }

  private def diffReport(found: String, expected: String): String = {
    import _root_.munit.diff.Diff
    val diff = new Diff(
      obtained = found,
      expected = expected
    )
    diff.createReport("Snapshot not equal", printObtainedAsStripMargin = false)
  }
}
