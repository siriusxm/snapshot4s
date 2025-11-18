package snapshot4s
package internals

import org.typelevel.scalaccompat.annotation.unused

private[snapshot4s] trait MultiLineReprCompat {
  private[snapshot4s] def productElementNames(@unused p: Product): Iterator[String] =
    Iterator.continually("")
  private[snapshot4s] def collectionClassName(i: Iterable[?]): String =
    i.stringPrefix

}
