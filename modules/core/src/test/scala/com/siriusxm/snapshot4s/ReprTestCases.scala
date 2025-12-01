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
    val repr  = getRepr[SingleField]
    val input = SingleField(42)
    expect.same("SingleField(x = 42)", repr.toSourceString(input))
  }

  test("Repr handles case class with many fields") {
    val repr  = getRepr[ManyFields]
    val input = ManyFields(1, "hello", true, 999L)
    expect.same("ManyFields(a = 1, b = \"hello\", c = true, d = 999L)", repr.toSourceString(input))
  }

  test("Repr handles nested case classes") {
    val repr  = getRepr[NestedCase]
    val input = NestedCase(SingleField(99))
    expect.same("NestedCase(inner = SingleField(x = 99))", repr.toSourceString(input))
  }

  test("Repr handles case class with Option field") {
    val repr      = getRepr[WithOption]
    val noneInput = WithOption(None)
    expect.same("WithOption(value = None)", repr.toSourceString(noneInput))
  }

  test("Repr handles case class with Either field") {
    val repr       = getRepr[WithEither]
    val leftInput  = WithEither(Left("error"))
    val rightInput = WithEither(Right(42))

    val leftOutput  = repr.toSourceString(leftInput)
    val rightOutput = repr.toSourceString(rightInput)
    expect.same(
      """WithEither(value = Left("error"))""",
      leftOutput
    ) &&
    expect.same(
      "WithEither(value = Right(42))",
      rightOutput
    )
  }

  test("Repr handles sealed trait with case class") {
    val repr  = getRepr[ComplexAdt]
    val input = DataCase(10, "data")
    expect.same("DataCase(x = 10, y = \"data\")", repr.toSourceString(input))
  }

  test("Repr handles sealed trait with case object") {
    val repr              = getRepr[ComplexAdt]
    val input: ComplexAdt = EmptyCase
    expect.same("EmptyCase", repr.toSourceString(input))
  }

  test("Repr handles special characters in values") {
    val repr  = getRepr[SpecialChars]
    val input = SpecialChars("Hello\nWorld", -42)
    expect.same(
      "SpecialChars(text = \"\"\"Hello\nWorld\"\"\", number = -42)",
      repr.toSourceString(input)
    )
  }

  test("Repr respects custom Repr instances for fields") {
    case class WithString(name: String)

    /* CustomReprScope is defined to host the implicit value, to avoid getting:
      local val customStringRepr in value <local ReprTestCases> is never used
         implicit val customStringRepr: Repr[String] = s => s"CustomString($s)"
     */
    object CustomReprScope {
      implicit val customStringRepr: Repr[String] = customReprs.string
      val repr                                    = implicitly[Repr[WithString]]
    }
    import CustomReprScope._

    val input = WithString("test")
    expect.same("WithString(name = CustomString(test))", repr.toSourceString(input))
  }

  test("Repr respects custom Repr instances for lists") {
    implicit val customStringRepr: Repr[String] = customReprs.string
    val repr                                    = implicitly[Repr[List[String]]]

    val input = List("test")
    expect.same("List(CustomString(test))", repr.toSourceString(input))
  }

  test("Repr respects custom Repr instances for seq") {
    implicit val customStringRepr: Repr[String] = customReprs.string
    val repr                                    = implicitly[Repr[Seq[String]]]

    val input = Seq("test")
    expect.same("Seq(CustomString(test))", repr.toSourceString(input))
  }

  test("Repr respects custom Repr instances for option") {
    implicit val customStringRepr: Repr[String] = customReprs.string
    val repr                                    = implicitly[Repr[Option[String]]]

    val input = Some("test")
    expect.same("Some(CustomString(test))", repr.toSourceString(input))
  }

  test("Repr respects custom Repr instances for either") {
    implicit val customStringRepr: Repr[String] = customReprs.string
    val repr                                    = implicitly[Repr[Either[String, String]]]

    val inputR = Right("test")
    val inputL = Left("err")

    expect.same("Right(CustomString(test))", repr.toSourceString(inputR)) &&
    expect.same("Left(CustomString(test))", repr.toSourceString(inputL))
  }

  test("Repr respects custom Repr instances for map") {
    implicit val customStringRepr: Repr[String] = customReprs.string
    val repr                                    = implicitly[Repr[Map[String, Int]]]

    val input = Map("test" -> 42)
    expect.same("Map(CustomString(test) -> 42)", repr.toSourceString(input))
  }

  object customReprs {
    implicit val string: Repr[String] = _ => "CustomString(test)"
  }

  protected def getRepr[T](implicit repr: Repr[T]): Repr[T] = repr
}
