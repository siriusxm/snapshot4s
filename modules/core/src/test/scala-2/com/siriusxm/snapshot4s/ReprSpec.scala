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

import munit.internal.MacroCompat
import weaver.*

object ReprSpec extends FunSuite with MacroCompat.CompileErrorMacro with ReprTestCases {

  case class MyCaseClass(a: String, b: Long)

  sealed trait MyAdt
  case object A extends MyAdt
  case object B extends MyAdt

  class RegularClass

  test("obtain Repr instance for case class") {
    val errors = compileErrors("""
    implicitly[Repr[MyCaseClass]]
    """)
    expect(errors.isEmpty())
  }

  test("obtain Repr instance for sealed trait") {
    val errors = compileErrors("""
    implicitly[Repr[MyAdt]]
    """)
    expect(errors.isEmpty())
  }

  test("obtain Repr instance for either") {
    val errors = compileErrors("""
    implicitly[Repr[Either[MyAdt, MyCaseClass]]]
    """)
    expect(errors.isEmpty())

  }

  test("obtain Repr instance for option") {
    val errors = compileErrors("""
    implicitly[Repr[Option[MyAdt]]]
    """)
    expect(errors.isEmpty())
  }

  test("fail to obtain Repr instance for regular class") {
    val errors = compileErrors("""
    implicitly[Repr[RegularClass]]
    """)
    expect(!errors.isEmpty())
  }

  test("Repr respects custom Repr instances for fields") {
    case class WithString(name: String)

    /* CustomReprScope is defined to host the implicit value, to avoid getting:
      local val customStringRepr in value <local ReprTestCases> is never used
         implicit val customStringRepr: Repr[String] = s => s"CustomString($s)"
     */
    object CustomReprScope {
      implicit val customStringRepr: Repr[String] = _ => "CustomString(test)"
      @annotation.nowarn("msg=match may not be exhaustive")
      val repr = implicitly[Repr[WithString]]
    }
    import CustomReprScope._

    val input = WithString("test")
    expect.same("WithString(name = CustomString(test))", repr.toSourceString(input))
  }

  test("Repr respects custom Repr instances for lists") {
    object CustomReprScope {
      implicit val customStringRepr: Repr[String] = _ => "CustomString(test)"
      @annotation.nowarn("msg=match may not be exhaustive")
      val repr = implicitly[Repr[List[String]]]
    }
    import CustomReprScope._

    val input = List("test")
    expect.same("""List(
    |CustomString(test)
    |)""".stripMargin, repr.toSourceString(input))
  }

  test("Repr respects custom Repr instances for seq") {
    object CustomReprScope {
      implicit val customStringRepr: Repr[String] = _ => "CustomString(test)"
      @annotation.nowarn("msg=match may not be exhaustive")
      val repr = implicitly[Repr[Seq[String]]]
    }
    import CustomReprScope._

    val input = Seq("test")
    expect.same("""Seq(
    |CustomString(test)
    |)""".stripMargin, repr.toSourceString(input))
  }

  test("Repr respects custom Repr instances for option") {
    object CustomReprScope {
      implicit val customStringRepr: Repr[String] = _ => "CustomString(test)"
      @annotation.nowarn("msg=match may not be exhaustive")
      val repr = implicitly[Repr[Option[String]]]
    }
    import CustomReprScope._

    val input = Some("test")
    expect.same("""Some(
    |CustomString(test)
    |)""".stripMargin, repr.toSourceString(input))
  }
  test("Repr respects custom Repr instances for either") {
    object CustomReprScope {
      implicit val customStringRepr: Repr[String] = _ => "CustomString(test)"
      @annotation.nowarn("msg=match may not be exhaustive")
      val repr = implicitly[Repr[Either[String, String]]]
    }
    import CustomReprScope._

    val inputR = Right("test")
    val inputL = Left("err")

    expect.same("""Right(
    |CustomString(test)
    |)""".stripMargin, repr.toSourceString(inputR)) &&
      expect.same("""Left(
        |CustomString(test)
        |)""".stripMargin, repr.toSourceString(inputL))
  }

}
