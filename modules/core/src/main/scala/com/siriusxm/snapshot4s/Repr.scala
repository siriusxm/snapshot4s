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

import scala.annotation.implicitNotFound

import snapshot4s.internals.InlineRepr

/** Repr provides a code representation for given type.
  * It serves the purpose of serializing data structure
  * into sources when building new snapshot candidate.
  */
@implicitNotFound(
  "Could not find implicit instance for Repr[${A}]. " +
    "This usually means that ${A} or it's component is not an ADT or primitive type. " +
    "In that case provide your own given instance of Repr[${A}]. " +
    "Repr.fromPprint can be used to generate it"
)
trait Repr[A] {
  def toSourceString(a: A): String
}

object Repr extends ReprForAdt {

  implicit val reprForString: Repr[String] = default
  implicit val reprForChar: Repr[Char]     = default
  implicit val reprForUnit: Repr[Unit]     = default

  implicit val reprForBoolean: Repr[Boolean] = default
  implicit val reprForShort: Repr[Short]     = default
  implicit val reprForInt: Repr[Int]         = default
  implicit val reprForLong: Repr[Long]       = default
  implicit val reprForFloat: Repr[Float]     = default
  implicit val reprForDouble: Repr[Double]   = default

  implicit def reprForIterable[A](implicit ev: Repr[A]): Repr[Iterable[A]] =
    reprForCollection[A, Iterable]
  implicit def reprForSeq[A](implicit ev: Repr[A]): Repr[Seq[A]] =
    reprForCollection[A, Seq]("Seq")
  implicit def reprForList[A](implicit ev: Repr[A]): Repr[List[A]] =
    reprForCollection[A, List]

  implicit def reprForArray[A](implicit ev: Repr[A]): Repr[Array[A]] = new Repr[Array[A]] {
    def toSourceString(x: Array[A]): String = iteratorToSourceString(x.iterator, "Array")
  }

  implicit def reprForVector[A](implicit ev: Repr[A]): Repr[Vector[A]] =
    reprForCollection[A, Vector]

  implicit def reprForOption[A](implicit reprA: Repr[A]): Repr[Option[A]] =
    new Repr[Option[A]] {
      def toSourceString(optA: Option[A]): String = optA match {
        case None    => "None"
        case Some(a) => s"Some(${reprA.toSourceString(a)})"
      }
    }

  implicit def reprForMap[K, V](implicit evK: Repr[K], evV: Repr[V]): Repr[Map[K, V]] =
    new Repr[Map[K, V]] {
      def toSourceString(x: Map[K, V]): String = mapToSourceString(x)
    }

  implicit def reprForEither[L, R](implicit
      evL: Repr[L],
      evR: Repr[R]
  ): Repr[Either[L, R]] = new Repr[Either[L, R]] {
    def toSourceString(x: Either[L, R]): String =
      x match {
        case Left(err)    => s"Left(${evL.toSourceString(err)})"
        case Right(value) => s"Right(${evR.toSourceString(value)})"
      }
  }

  private def reprForCollection[A, Collection[A] <: Iterable[A]](
      classNameOverride: String
  )(implicit ev: Repr[A]) =
    new Repr[Collection[A]] {
      def toSourceString(x: Collection[A]): String =
        iteratorToSourceString(x.iterator, classNameOverride)
    }

  private def reprForCollection[A, Collection[A] <: Iterable[A]](implicit ev: Repr[A]) =
    new Repr[Collection[A]] {
      def toSourceString(x: Collection[A]): String =
        iteratorToSourceString(x.iterator, InlineRepr.collectionClassName(x))
    }

  private def iteratorToSourceString[A](x: Iterator[A], className: String)(implicit
      ev: Repr[A]
  ): String =
    InlineRepr.printApply[A](
      className,
      x
    )(value => ev.toSourceString(value))

  private def mapToSourceString[K, V](x: Map[K, V])(implicit
      evK: Repr[K],
      evV: Repr[V]
  ): String =
    InlineRepr.printApply[(K, V)](
      "Map",
      x.toList.iterator
    )(kv => evK.toSourceString(kv._1) + " -> " + evV.toSourceString(kv._2))

  // Creates Repr instance based on pprint
  @deprecated(
    "This method is no longer recommended and will be removed in future releases. Use the derivation instead.",
    "0.2.4"
  )
  def fromPprint[A]: Repr[A] = (a: A) =>
    // width and height are overridden to handle very large snapshots
    pprint.apply(a, width = 200, height = 99999999).plainText

  private def default[A]: Repr[A] = InlineRepr.repr[A]

}
