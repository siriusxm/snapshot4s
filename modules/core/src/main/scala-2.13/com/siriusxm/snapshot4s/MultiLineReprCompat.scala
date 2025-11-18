package snapshot4s
package internals

private[snapshot4s] trait MultiLineReprCompat {
  private[snapshot4s] def productElementNames(p: Product): Iterator[String] =
    p.productElementNames

  private[snapshot4s] def collectionClassName(i: Iterable[?]): String = i
    .asInstanceOf[{ def collectionClassName: String }]
    .collectionClassName

}
