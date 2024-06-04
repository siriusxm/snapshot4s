package snapshot4s

import weaver.*

object AnnotationSpec extends FunSuite {

  test("adds an annotation for potential interpolators") {
    val code      = """"${x}""""
    val annotated = """"${x}": @scala.annotation.nowarn("msg=possible missing interpolator")"""
    expect.eql(annotated, InlineSnapshot.addNowarnAnnotations(code))
  }

  test("adds an annotation for potential interpolated identifiers") {
    val code      = """"$x""""
    val annotated = """"$x": @scala.annotation.nowarn("msg=possible missing interpolator")"""
    expect.eql(annotated, InlineSnapshot.addNowarnAnnotations(code))
  }

  test("does not add an annotation for basic strings") {
    val code = """"x""""
    expect.eql(code, InlineSnapshot.addNowarnAnnotations(code))
  }

}
