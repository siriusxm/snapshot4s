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
