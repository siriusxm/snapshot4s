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

import scala.compiletime.{constValue, constValueTuple, error, summonAll, summonFrom}
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
    val elemInstances = summonAll[Tuple.Map[m.MirroredElemTypes, Repr]]

    inline m match
      case s: Mirror.SumOf[A]     => derivedSum(s, elemInstances)
      case p: Mirror.ProductOf[A] => derivedProduct(p, elemInstances)

  private inline def derivedProduct[A](mirror: Mirror.ProductOf[A], elemInstances: Tuple): Repr[A] =
    val typeName   = constValue[mirror.MirroredLabel]
    val elemLabels = constValueTuple[mirror.MirroredElemLabels]

    // Extract to avoid duplication warning in inline context
    def createProductRepr(typeName: String, elemLabels: Tuple, elemInstances: Tuple): Repr[A] =
      new Repr[A]:
        def toSourceString(a: A): String =
          val product  = a.asInstanceOf[Product]
          val elements = product.productIterator.toArray

          if elements.isEmpty then typeName
          else
            val reprInstances = elemInstances.toArray.asInstanceOf[Array[Repr[Any]]]
            val elementReprs  = elements.zip(reprInstances).map { case (elem, repr) =>
              repr.toSourceString(elem)
            }

            val labelsList = elemLabels.toArray.asInstanceOf[Array[String]]
            if labelsList.nonEmpty && labelsList.forall(_.nonEmpty) then
              // Named parameters: Person(name = "John", age = 25)
              val namedArgs = labelsList.zip(elementReprs).map { case (label, repr) =>
                s"$label = $repr"
              }
              s"$typeName(${namedArgs.mkString(", ")})"
            else
              // Positional parameters: Person("John", 25)
              s"$typeName(${elementReprs.mkString(", ")})"

    createProductRepr(typeName, elemLabels, elemInstances)

  end derivedProduct

  private inline def derivedSum[A](mirror: Mirror.SumOf[A], elemInstances: Tuple): Repr[A] =
    // Extract to avoid duplication warning in inline context
    def createSumRepr(mirror: Mirror.SumOf[A], elemInstances: Tuple): Repr[A] =
      new Repr[A]:
        def toSourceString(a: A): String =
          val ordinal      = mirror.ordinal(a)
          val reprInstance = elemInstances.productElement(ordinal).asInstanceOf[Repr[Any]]
          reprInstance.toSourceString(a)

    createSumRepr(mirror, elemInstances)
end ReprForAdt
