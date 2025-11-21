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

import scala.annotation.nowarn

import weaver.*

/** Shared test cases for Repr derivation that work across Scala 2 and 3.
  * This trait should be mixed into version-specific ReprSpec objects.
  */
trait ReprTestCases { self: FunSuite =>

  case class Empty()
  case class SingleField(x: Int)
  case class ManyFields(a: Int, b: String, c: Boolean, d: Long)
  case class NestedCase(inner: SingleField)
  case class WithOption(value: Option[String])
  case class WithEither(value: Either[String, Int])

  sealed trait ComplexAdt
  case class DataCase(x: Int, y: String) extends ComplexAdt
  case object EmptyCase                  extends ComplexAdt

  case class SpecialChars(text: String, number: Int)

  test("Repr handles empty case class") {
    val repr  = getRepr[Empty]
    val input = Empty()
    expect.same("Empty", repr.toSourceString(input))
  }

  test("Repr handles single field case class") {
    @nowarn("msg=match may not be exhaustive")
    val repr  = getRepr[SingleField]
    val input = SingleField(42)
    expect.same("SingleField(x = 42)", repr.toSourceString(input))
  }

  test("Repr handles case class with many fields") {
    @nowarn("msg=match may not be exhaustive")
    val repr  = getRepr[ManyFields]
    val input = ManyFields(1, "hello", true, 999L)
    expect.same("ManyFields(a = 1, b = \"hello\", c = true, d = 999L)", repr.toSourceString(input))
  }

  test("Repr handles nested case classes") {
    @nowarn("msg=match may not be exhaustive")
    val repr  = getRepr[NestedCase]
    val input = NestedCase(SingleField(99))
    expect.same("NestedCase(inner = SingleField(x = 99))", repr.toSourceString(input))
  }

  test("Repr handles case class with Option field") {
    @nowarn("msg=match may not be exhaustive")
    val repr      = getRepr[WithOption]
    val noneInput = WithOption(None)
    expect.same("WithOption(value = None)", repr.toSourceString(noneInput))
  }

  test("Repr handles case class with Either field") {
    @nowarn("msg=match may not be exhaustive")
    val repr       = getRepr[WithEither]
    val leftInput  = WithEither(Left("error"))
    val rightInput = WithEither(Right(42))

    val leftOutput  = repr.toSourceString(leftInput)
    val rightOutput = repr.toSourceString(rightInput)
    expect.same("""WithEither(value = Left(
    |value = "error"
    |))""".stripMargin, leftOutput) &&
    expect.same("""WithEither(value = Right(
    |value = 42
    |))""".stripMargin, rightOutput) 
  }

  test("Repr handles sealed trait with case class") {
    @nowarn("msg=match may not be exhaustive")
    val repr  = getRepr[ComplexAdt]
    val input = DataCase(10, "data")
    expect.same("DataCase(x = 10, y = \"data\")", repr.toSourceString(input))
  }

  test("Repr handles sealed trait with case object") {
    @nowarn("msg=match may not be exhaustive")
    val repr              = getRepr[ComplexAdt]
    val input: ComplexAdt = EmptyCase
    expect.same("EmptyCase", repr.toSourceString(input))
  }

  test("Repr handles special characters in values") {
    @nowarn("msg=match may not be exhaustive")
    val repr  = getRepr[SpecialChars]
    val input = SpecialChars("Hello\nWorld", -42)
    expect.same(
      "SpecialChars(text = \"\"\"Hello\nWorld\"\"\", number = -42)",
      repr.toSourceString(input)
    )
  }

  test("Repr uses named parameters for case classes") {
    @nowarn("msg=match may not be exhaustive")
    val repr   = getRepr[ManyFields]
    val input  = ManyFields(0, "", false, 0L)
    val output = repr.toSourceString(input)

    expect(output.contains(" = ")) &&
    expect.same("ManyFields(a = 0, b = \"\", c = false, d = 0L)", output)
  }

  test("Repr handles special characters in string values") {
    @nowarn("msg=match may not be exhaustive")
    val repr  = getRepr[SingleField]
    val input = SingleField(-42)
    expect.same("SingleField(x = -42)", repr.toSourceString(input))
  }

  protected def getRepr[T](implicit repr: Repr[T]): Repr[T] = repr
}
