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

trait ReprForAdt {

  // Mirror is only used to guarantee we are working with sum/product
  // in such case it's safe to rely on pprint instead of having a separate Repr implementation
  implicit def productRepr[A <: Product]: Repr[A] = v =>
    // width and height are overridden to handle very large snapshots
    pprint.apply(v, width = 200, height = 99999999).plainText
  implicit def sumRepr[A]: Repr[A] = macro ReprForSum.impl[A]

}

private[snapshot4s] object ReprForSum {

  def impl[A: c.WeakTypeTag](c: blackbox.Context): c.Expr[Repr[A]] = {
    import c.universe.*

    val weakTypeOfA = weakTypeOf[A]
    val isSealed    = weakTypeOfA.typeSymbol.isClass && weakTypeOfA.typeSymbol.asClass.isSealed

    if (!isSealed) {
      c.abort(
        c.enclosingPosition,
        s"Failed to generate Repr for sum because $weakTypeOfA is not a sealed trait."
      )
    } else {
      val subtypes = weakTypeOfA.typeSymbol.asClass.knownDirectSubclasses
      if (subtypes.size > 0) {
        reify { Repr.fromPprint[A] }
      } else {
        c.abort(
          c.enclosingPosition,
          s"Failed to generate Repr for sum because sealed trait $weakTypeOfA has no subtypes."
        )
      }
    }

  }
}
