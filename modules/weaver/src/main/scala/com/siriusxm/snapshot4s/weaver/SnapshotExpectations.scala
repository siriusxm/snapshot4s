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

import _root_.weaver.{Expectations, SourceLocation}
import cats.effect.IO

import snapshot4s.*

private[weaver] trait LowPrioritySnapshotEq {

  implicit def snapshotEqForUniversalEquals[A]: SnapshotEq[A] = SnapshotEq.fromUniversalEquals[A]
}

private[weaver] trait SnapshotEqInstances extends LowPrioritySnapshotEq {

  implicit def snapshotEqForCasEq[A](implicit eq: cats.Eq[A]): SnapshotEq[A] =
    SnapshotEq.instance(eq.eqv)
}

trait SnapshotExpectations extends SnapshotAssertions[IO[Expectations]] with SnapshotEqInstances {

  implicit def weaverResultLike[A](implicit loc: SourceLocation): ResultLike[A, IO[Expectations]] =
    WeaverResultLike.resultLike[A](loc)
}

object SnapshotExpectations extends SnapshotExpectations
