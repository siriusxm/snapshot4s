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

  /** Main derivation entry point. Attempts to derive Repr for sum or product types.
    * This is the Scala 2 equivalent of the Scala 3 summoningFrom method.
    */
  implicit def derived[A]: Repr[A] = macro ReprForAdtMacros.deriveRepr[A]

}

private[snapshot4s] object ReprForAdtMacros {

  def deriveRepr[A: c.WeakTypeTag](c: blackbox.Context): c.Expr[Repr[A]] = {
    import c.universe._

    val tpe    = weakTypeOf[A]
    val symbol = tpe.typeSymbol

    if (!symbol.isClass) {
      c.abort(
        c.enclosingPosition,
        s"""Cannot derive Repr instance. The type $tpe is not a class.

See the guide for a list of supported types:
https://siriusxm.github.io/snapshot4s/inline-snapshots/#supported-data-types"""
      )
    }

    val classSymbol = symbol.asClass

    if (classSymbol.isSealed) {
      // Handle sealed trait/class (sum type)
      deriveSumRepr[A](c)
    } else if (classSymbol.isCaseClass) {
      // Handle case class (product type)
      deriveProductRepr[A](c)
    } else {
      c.abort(
        c.enclosingPosition,
        s"""Cannot derive Repr instance. The type $tpe is neither a Sum nor Product type.

See the guide for a list of supported types:
https://siriusxm.github.io/snapshot4s/inline-snapshots/#supported-data-types"""
      )
    }
  }

  private def deriveProductRepr[A: c.WeakTypeTag](c: blackbox.Context): c.Expr[Repr[A]] = {
    import c.universe._

    val tpe         = weakTypeOf[A]
    val typeName    = tpe.typeSymbol.name.decodedName.toString
    val classSymbol = tpe.typeSymbol.asClass

    // Get constructor parameters
    val constructor = classSymbol.primaryConstructor.asMethod
    val paramLists  = constructor.paramLists

    if (paramLists.isEmpty || paramLists.head.isEmpty) {
      // No parameters - simple case
      c.Expr[Repr[A]](q"""
        new _root_.snapshot4s.Repr[$tpe] {
          def toSourceString(a: $tpe): String = ${typeName}
        }
      """)
    } else {
      val params = paramLists.head

      // Generate field access and Repr instance summoning for each parameter
      val fieldAccessors = params.zipWithIndex.map { case (param, index) =>
        val paramName = param.name.decodedName.toString
        val paramType = param.typeSignature.finalResultType

        (paramName, paramType, index)
      }

      // Generate match cases for each field type
      val matchCases = fieldAccessors.map { case (_, paramType, index) =>
        cq"""_ if index == $index => implicitly[_root_.snapshot4s.Repr[$paramType]].toSourceString(elem.asInstanceOf[$paramType])"""
      }

      // Generate field name array
      val fieldNameLiterals = fieldAccessors.map { case (paramName, _, _) => q"$paramName" }

      // Create the implementation
      c.Expr[Repr[A]](q"""
        new _root_.snapshot4s.Repr[$tpe] {
          def toSourceString(a: $tpe): String = {
            val product = a.asInstanceOf[Product]
            val elements = product.productIterator.toArray

            if (elements.isEmpty) {
              $typeName
            } else {
              // Get representations for each element
              val elementReprs = elements.zipWithIndex.map { case (elem, index) =>
                elem match {
                  case ..$matchCases
                }
              }

              // Field names for named parameter format
              val fieldNames = Array(..$fieldNameLiterals)

              if (fieldNames.nonEmpty && fieldNames.forall(_.nonEmpty)) {
                // Named parameters: Person(name = "John", age = 25)
                val namedArgs = fieldNames.zip(elementReprs).map { case (label, repr) =>
                  label + " = " + repr
                }
                $typeName + "(" + namedArgs.mkString(", ") + ")"
              } else {
                // Positional parameters: Person("John", 25)
                $typeName + "(" + elementReprs.mkString(", ") + ")"
              }
            }
          }
        }
      """)
    }
  }

  private def deriveSumRepr[A: c.WeakTypeTag](c: blackbox.Context): c.Expr[Repr[A]] = {
    import c.universe._

    val tpe         = weakTypeOf[A]
    val classSymbol = tpe.typeSymbol.asClass

    if (!classSymbol.isSealed) {
      c.abort(c.enclosingPosition, s"Cannot derive sum type Repr for $tpe because it is not sealed")
    }

    val knownSubclasses = classSymbol.knownDirectSubclasses.toList
    if (knownSubclasses.isEmpty) {
      c.abort(
        c.enclosingPosition,
        s"Cannot derive sum type Repr for sealed trait $tpe because it has no subclasses"
      )
    }

    // Create cases for each subclass
    val cases = knownSubclasses.map { subclass =>
      val subType = subclass.asType.toType
      cq"""_: $subType => implicitly[_root_.snapshot4s.Repr[$subType]].toSourceString(a.asInstanceOf[$subType])"""
    }

    c.Expr[Repr[A]](q"""
      new _root_.snapshot4s.Repr[$tpe] {
        def toSourceString(a: $tpe): String = {
          a match {
            case ..$cases
          }
        }
      }
    """)
  }
}
