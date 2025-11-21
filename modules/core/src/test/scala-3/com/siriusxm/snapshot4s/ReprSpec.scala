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

import scala.compiletime.testing.*

import weaver.*

object ReprSpec extends FunSuite with ReprTestCases {

  case class MyCaseClass(a: String, b: Long)

  sealed trait MyAdt
  case object A extends MyAdt
  case object B extends MyAdt

  enum MyEnum {
    case X
    case Y
  }

  compilesWithoutError("obtain Repr instance for case class") {
    """
    summon[Repr[MyCaseClass]]
    """
  }

  compilesWithoutError("obtain Repr instance for case class") {
    """
    summon[Repr[MyCaseClass]]
    """
  }

  compilesWithoutError("obtain Repr instance for enum") {
    """
    summon[Repr[MyAdt]]
    summon[Repr[MyEnum]]
    """
  }

  compilesWithoutError("obtain Repr instance for either") {
    """
    summon[Repr[Either[MyEnum, MyCaseClass]]]
    """
  }

  compilesWithoutError("obtain Repr instance for option") {
    """
    summon[Repr[Option[MyAdt]]]
    """
  }

  failsCompilation("obtain Repr instance for Object") {
    """
    summon[Repr[Object]]
    """
  }

  failsCompilationWith("obtain Repr instance for Throwable") {
    """
    Repr.summoningFrom[Throwable]
    """
  }("Cannot derive Repr instance. The type parameter is neither a Sum nor Product type")

  failsCompilationWith("obtain Repr instance for either with throwable") {
    """
    summon[Repr[Either[Throwable, String]]]
    """
  }("Could not find implicit instance for Repr[Either[Throwable, String]]")

  enum MyRecursiveADT {
    case Recursive(a: MyRecursiveADT)
    case Base
  }

  failsCompilationWith("obtain Repr instance for recursive enum") {
    """
    summon[Repr[MyRecursiveADT]]
    """
  }(
    "Could not find implicit instance for Repr[snapshot4s.ReprSpec.MyRecursiveADT]"
  )

  case class RecursiveProduct(data: RecursiveProduct)

  failsCompilationWith("obtain Repr instance for recursive case class") {
    """
    Repr.derived[RecursiveProduct]
    """
  }(
    "Could not find implicit instance for Repr[snapshot4s.ReprSpec.RecursiveProduct]"
  )

  test("Repr respects custom Repr instances for fields") {
    case class WithString(name: String)

    // Use Scala 3 given syntax
    given customStringRepr: Repr[String] = _ => "CustomString(test)"
    val repr                             = summon[Repr[WithString]]

    val input = WithString("test")
    expect.same("WithString(name = CustomString(test))", repr.toSourceString(input))
  }

  test("Repr respects custom Repr instances for lists") {
    given customStringRepr: Repr[String] = _ => "CustomString(test)"
    val repr                             = summon[Repr[List[String]]]

    val input = List("test")
    expect.same("""List(
    |CustomString(test)
    |)""".stripMargin, repr.toSourceString(input))
  }

  test("Repr respects custom Repr instances for seq") {
    given customStringRepr: Repr[String] = _ => "CustomString(test)"
    val repr                             = summon[Repr[Seq[String]]]

    val input = Seq("test")
    expect.same("""Seq(
    |CustomString(test)
    |)""".stripMargin, repr.toSourceString(input))
  }

  test("Repr respects custom Repr instances for option") {
    given customStringRepr: Repr[String] = _ => "CustomString(test)"
    val repr                             = summon[Repr[Option[String]]]

    val input = Some("test")
    expect.same("""Some(
    |CustomString(test)
    |)""".stripMargin, repr.toSourceString(input))
  }
  test("Repr respects custom Repr instances for either") {
    given customStringRepr: Repr[String] = _ => "CustomString(test)"
    val repr                             = summon[Repr[Either[String, String]]]

    val inputR = Right("test")
    val inputL = Left("err")

    expect.same("""Right(
    |CustomString(test)
    |)""".stripMargin, repr.toSourceString(inputR)) &&
      expect.same("""Left(
        |CustomString(test)
        |)""".stripMargin, repr.toSourceString(inputL))
  }

  private inline def compilesWithoutError(name: String)(inline code: String) =
    test(s"[compiles] $name") {
      val compilationResult = typeCheckErrors(code)
      expect(compilationResult.isEmpty, s"Failed to compile the code: $compilationResult")
    }

  private inline def failsCompilation(name: String)(inline code: String) =
    test(s"[doesn't compile] $name")(expect(!typeChecks(code)))

  private inline def failsCompilationWith(name: String)(inline code: String)(error: String) =
    test(s"[doesn't compile] $name") {
      val compilationResult = typeCheckErrors(code)
      val errorMessage      =
        if compilationResult.isEmpty then "Compilation didn't fail, although it was expected to"
        else s"Compilation failed but the error didn't match. Cause: $compilationResult"
      expect(compilationResult.exists(_.message.contains(error)), errorMessage)
    }

}
