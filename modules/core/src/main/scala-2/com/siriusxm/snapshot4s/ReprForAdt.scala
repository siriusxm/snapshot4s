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
      val params                 = paramLists.head
      val labelsAndReprInstances = params.map { param =>
        val label     = param.name.decodedName.toString
        val paramType = param.typeSignature.finalResultType.asSeenFrom(tpe, classSymbol)
        val repr      = q"implicitly[_root_.snapshot4s.Repr[$paramType]].asInstanceOf[Repr[Any]]"
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

    val cases = knownSubclasses.map { subclass =>
      val subType         = subclass.asType.toType
      val concreteSubType =
        if (shouldSubstituteGenericsInSubtype(c)(tpe, classSymbol, subType))
          substituteTypeParameters(c)(tpe, classSymbol, subType).getOrElse(subType)
        else subType

      cq"""_: $concreteSubType => implicitly[_root_.snapshot4s.Repr[$concreteSubType]].toSourceString(a.asInstanceOf[$concreteSubType])"""
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

  // For types like MyEither[String, Int] the subtype is MyLeft[A, B], and in the pattern match we need MyLeft[String, Int]
  // This function tells if substitution is necessary
  private def shouldSubstituteGenericsInSubtype(c: blackbox.Context)(
      parentType: c.Type,
      parentSymbol: c.universe.ClassSymbol,
      subType: c.Type
  ): Boolean = {
    import c.universe._

    val parentIsGeneric   = parentType.typeArgs.nonEmpty
    val subTypeSymbol     = subType.typeSymbol.asType
    val subclassIsGeneric = subTypeSymbol.typeParams.nonEmpty
    val baseType          = subType.baseType(parentSymbol)
    val baseTypeNotFound  = baseType == NoType
    parentIsGeneric && subclassIsGeneric && !baseTypeNotFound
  }

  private def substituteTypeParameters(c: blackbox.Context)(
      parentType: c.Type,
      parentSymbol: c.universe.ClassSymbol,
      subType: c.Type
  ): Option[c.Type] = {
    import c.universe._

    val subTypeSymbol = subType.typeSymbol.asType
    val subTypeParams = subTypeSymbol.typeParams
    val baseType      = subType.baseType(parentSymbol)

    // We want to go from Parent: MyEither[String, Int] to MyEither[A, B] to MyLeft[A, B] to MyLeft[String, Int]
    val concreteTypeArgs = subTypeParams.map { subTypeParam =>
      findConcreteTypeForParameter(c)(subTypeParam, baseType, parentType)
    }
    val hasUnresolvedTypeParameters = concreteTypeArgs.exists(_.typeSymbol.isParameter)

    if (!hasUnresolvedTypeParameters) Some(appliedType(subTypeSymbol, concreteTypeArgs))
    else None
  }

  private def findConcreteTypeForParameter(c: blackbox.Context)(
      subTypeParam: c.universe.Symbol,
      baseType: c.Type,
      parentType: c.Type
  ): c.Type = {
    val paramSymbol    = subTypeParam.asType.toType.typeSymbol
    val idx            = baseType.typeArgs.indexWhere(arg => arg.typeSymbol == paramSymbol)
    val parameterFound = idx >= 0
    val indexIsValid   = idx < parentType.typeArgs.length
    if (parameterFound && indexIsValid) parentType.typeArgs(idx)
    // If we can't find a mapping, keep the type parameter as-is
    else subTypeParam.asType.toType
  }
}
