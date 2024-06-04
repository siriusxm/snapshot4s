package snapshot4s

import munit.internal.MacroCompat
import weaver.*

object ReprSpec extends FunSuite with MacroCompat.CompileErrorMacro {

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

}
