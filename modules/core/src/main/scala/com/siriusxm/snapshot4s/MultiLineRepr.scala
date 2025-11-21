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

private[snapshot4s] object MultiLineRepr extends MultiLineReprCompat {

  private val open: String  = "("
  private val close: String = ")"
  private val comma: String = ","

  private[snapshot4s] def repr[A]: Repr[A] = (a: A) => {
    val out                             = new StringBuilder()
    def loop(a: Any): Unit = {
      a match {
        case null    => out.append("null")
        case x: Char =>
          out.append('\'')
          if (x == '\'') out.append("\\'") else printChar(x, out)
          out.append('\'')
        case x: Byte      => out.append(x.toString())
        case x: Short     => out.append(x.toString())
        case x: Int       => out.append(x.toString())
        case x: Long      => out.append(s"${x.toString()}L")
        case x: Float     => out.append(x.toString())
        case x: Double    => out.append(x.toString())
        case x: String    => printString(x, out)
        case None         => out.append("None")
        case Nil          => out.append("Nil")
        case x: Map[?, ?] =>
          printApply(
            collectionClassName(x),
            x.iterator,
            out
          ) { case (key, value) =>
            loop(key)
            out.append(" -> ")
            loop(value)
          }
        case x: Iterable[?] =>
          printApply(
            collectionClassName(x),
            x.iterator,
            out
          )(value => loop(value))
        case x: Array[?] =>
          printApply("Array", x.iterator, out)(value => loop(value))
        case it: Iterator[?] =>
          if (it.isEmpty) out.append("empty iterator")
          else out.append("non-empty iterator")
        case p: Product =>
          val elementNames         = productElementNames(p)
          val infiniteElementNames = Iterator
            .continually(if (elementNames.hasNext) elementNames.next() else "")
          printApply(
            p.productPrefix,
            p.productIterator.zip(infiniteElementNames),
            out
          ) { case (value, key) =>
            if (key.nonEmpty) out.append(key).append(" = "): Unit
            loop(value)
          }
        case _ => out.append(a.toString())
      }
    }
    loop(a)
    out.toString()
  }

  private[snapshot4s] def printApply[T](
      prefix: String,
      it: Iterator[T],
      out: StringBuilder,
  )(fn: T => Unit): Unit = {
    out.append(prefix)
    out.append(open)
    if (it.hasNext) {
      printNewline(out)
      while (it.hasNext) {
        val value = it.next()
        fn(value)
        if (it.hasNext) {
          out.append(comma)
          printNewline(out)
        } else printNewline(out)
      }
    }
    out.append(close)
  }

  private def printNewline(out: StringBuilder): Unit =
    out.append("\n")

  private def printString(
      string: String,
      out: StringBuilder
  ): Unit = {
    val isMultiline = string.contains('\n')
    if (isMultiline) {
      out.append('"')
      out.append('"')
      out.append('"')
      out.append(string)
      out.append('"')
      out.append('"')
      out.append('"')
    } else {
      out.append('"')
      var i = 0
      while (i < string.length()) {
        printChar(string.charAt(i), out)
        i += 1
      }
      out.append('"')
    }
  }

  private def printChar(
      c: Char,
      sb: StringBuilder
  ): Unit = c match {
    case '"'      => sb.append("\\\"")
    case '\\'     => sb.append("\\\\")
    case '\b'     => sb.append("\\b")
    case '\f'     => sb.append("\\f")
    case '\n'     => sb.append("\\n")
    case '\r'     => sb.append("\\r")
    case '\t'     => sb.append("\\t")
    case '\u001B' => ()
    case c        =>
      val isNonReadableAscii = c < ' ' || c > '~'
      if (isNonReadableAscii && !Character.isLetter(c))
        sb
          .append("\\u%04x".format(c.toInt))
      else sb.append(c)
  }

}
