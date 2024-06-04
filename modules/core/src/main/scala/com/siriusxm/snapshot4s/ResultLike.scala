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

import scala.annotation.implicitNotFound

/** A type class to convert a snapshot result computation into a framework-specific result.
  *  @tparam A The type value being compared
  *  @tparam R The framework-specific result type.
  */
@implicitNotFound(
  """Could not find an implicit instance for ResultLike[${A}, ${R}].

The ResultLike typeclass instance should be provided by the snapshot4s integration library. There may be a bug in the integration.
See https://siriusxm.github.io/snapshot4s/contributing/integrations/ for more information."""
)
trait ResultLike[A, R] {

  /** Lifts a suspended snapshot assertion into a framework-specific result.
    *
    * The assertion is side-effecting. It compares the values under test and may write files on failure.
    */
  def apply(result: () => Result[A]): R
}
