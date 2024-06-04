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

/** A type class used to compare a found value to a snapshot value.
  */
@implicitNotFound(
  """Could not find implicit instance of SnapshotEq[${A}].

Declare one with:

import snapshot4s.SnapshotEq

implicit val snapshotEq: SnapshotEq[${A}] = SnapshotEq.fromUniversalEquals

The SnapshotEq typeclass instance should be provided by the snapshot4s integration library. There may be a bug in the integration.
See https://siriusxm.github.io/snapshot4s/contributing/integrations/ for more information."""
)
trait SnapshotEq[A] {

  /** Compares a found and snapshot value. */
  def eqv(found: A, snapshot: A): Boolean
}

object SnapshotEq {

  /** Constructs a typeclass instance from a comparison function. */
  def instance[A](f: (A, A) => Boolean): SnapshotEq[A] = new SnapshotEq[A] {
    def eqv(found: A, snapshot: A): Boolean = f(found, snapshot)
  }

  def fromUniversalEquals[A]: SnapshotEq[A] = instance[A](_ == _)
}
