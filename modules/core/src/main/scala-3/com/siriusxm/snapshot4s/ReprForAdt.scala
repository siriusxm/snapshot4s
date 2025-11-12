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

import scala.compiletime.{error, summonAll, summonFrom}
import scala.deriving.Mirror

trait ReprForAdt:

  inline given summoningFrom[A]: Repr[A] =
    summonFrom {
      case given Mirror.Of[A] => derived[A]
      case _                  =>
        error("""Cannot derive Repr instance. The type parameter is neither a Sum nor Product type.

See the guide for a list of supported types:
https://siriusxm.github.io/snapshot4s/inline-snapshots/#supported-data-types""")
    }

  inline def derived[A](using m: Mirror.Of[A]): Repr[A] =
    // we don't care about the actual instances
    // summon is called to make sure all member types
    // also have their Repr instances
    summonAll[Tuple.Map[m.MirroredElemTypes, Repr]]
    Repr.fromPprint
