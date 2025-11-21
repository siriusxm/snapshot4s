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
      deriveSumRepr[A](c)
    } else if (classSymbol.isCaseClass) {
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

    val constructor = classSymbol.primaryConstructor.asMethod
    val paramLists  = constructor.paramLists

    if (paramLists.isEmpty || paramLists.head.isEmpty) {
      c.Expr[Repr[A]](q"""
        new _root_.snapshot4s.Repr[$tpe] {
          def toSourceString(a: $tpe): String = ${typeName}
        }
      """)
    } else {
      val params = paramLists.head
      val labelsAndReprInstances = params.map { param =>
        val label = param.name.decodedName.toString
        val paramType = param.typeSignature.finalResultType
        val repr = q"implicitly[_root_.snapshot4s.Repr[$paramType]].asInstanceOf[Repr[Any]]"
        q"($label, $repr)"
      }
      c.Expr[Repr[A]](q"""
        new _root_.snapshot4s.Repr[$tpe] {
          def toSourceString(a: $tpe): String = {
            val product = a.asInstanceOf[Product]
            val elements = product.productIterator.toList
            val labelsAndReprInstances = List(..$labelsAndReprInstances)
              val namedArgs = elements.zip(labelsAndReprInstances).map { case (elem, (label, repr)) =>
                label + " = " + repr.toSourceString(elem)
              }
              $typeName + "(" + namedArgs.mkString(", ") + ")"
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
