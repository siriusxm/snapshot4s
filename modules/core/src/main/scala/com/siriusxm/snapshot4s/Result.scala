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

/** A datatype describing the result of an assertion. */
sealed trait Result[A]

object Result {

  /** The comparison between the found value and snapshot value succeeded. */
  case class Success[A](found: A, snapshot: A) extends Result[A]

  /** The snapshot value does not exist. Most likely, it has not been generated yet. */
  case class NonExistent[A](found: A) extends Result[A]

  /** The comparison between the found value and snapshot value failed. */
  case class Failure[A](found: A, snapshot: A) extends Result[A]
}
