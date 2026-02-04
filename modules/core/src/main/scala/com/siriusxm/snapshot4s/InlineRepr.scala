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

// Adaptation of https://github.com/scalameta/munit/blob/00b41cea78bd6b253f21f0b12e6382d480279dcc/munit/shared/src/main/scala/munit/internal/console/Printers.scala
package snapshot4s
package internals

private[snapshot4s] object InlineRepr extends InlineReprCompat {

  private val open: String  = "("
  private val close: String = ")"
  private val comma: String = ","
  private val quote: Char   = '\"'

  private[snapshot4s] def repr[A]: Repr[A] = {
    case null    => "null"
    case x: Char =>
      val body = if (x == '\'') "\\'" else printChar(x)
      s"\'${(body)}\'"
    case x: Byte   => x.toString()
    case x: Short  => x.toString()
    case x: Int    => x.toString()
    case x: Long   => s"${x.toString()}L"
    case x: Float  => x.toString()
    case x: Double => x.toString()
    case x: String => printString(x)
    case None      => "None"
    case Nil       => "Nil"
    case a         => a.toString()
  }

  private[snapshot4s] def printApply[T](
      prefix: String,
      it: Iterator[T]
  )(fn: T => String): String = {
    val body = it.map(fn).mkString(comma)
    s"$prefix$open$body$close"
  }

  private def printString(
      string: String
  ): String = {
    val isMultiline    = string.contains('\n')
    val hasTripleQuote = string.contains("\"\"\"")
    if (isMultiline && !hasTripleQuote) printTripleQuotedString(string)
    else printQuotedString(string)
  }

  private def printTripleQuotedString(string: String): String = {
    val out = new StringBuilder()
    out.append(quote).append(quote).append(quote)
    string.foreach(out.append)
    out.append(quote).append(quote).append(quote)
    out.toString()
  }

  private def printQuotedString(string: String): String = {
    val out = new StringBuilder()
    out.append(quote)
    string.map(printChar).foreach(out.append)
    out.append(quote)
    out.toString()
  }

  private def printChar(
      c: Char
  ): String = c match {
    case '"'      => "\\\""
    case '\\'     => "\\\\"
    case '\b'     => "\\b"
    case '\f'     => "\\f"
    case '\n'     => "\\n"
    case '\r'     => "\\r"
    case '\t'     => "\\t"
    case '\u001B' => ""
    case c        =>
      val isNonReadableAscii = c < ' ' || c > '~'
      if (isNonReadableAscii && !Character.isLetter(c))
        "\\u%04x".format(c.toInt)
      else c.toString
  }
}
