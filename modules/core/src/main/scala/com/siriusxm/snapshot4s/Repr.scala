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

import org.typelevel.scalaccompat.annotation.unused

import snapshot4s.internals.MultiLineRepr

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

  implicit def reprForIterable[A](implicit @unused ev: Repr[A]): Repr[Iterable[A]] =
    reprForCollection[A, Iterable]
  implicit def reprForSeq[A](implicit @unused ev: Repr[A]): Repr[Seq[A]] =
    reprForCollection[A, Seq]("Seq")
  implicit def reprForList[A](implicit @unused ev: Repr[A]): Repr[List[A]] =
    reprForCollection[A, List]

  implicit def reprForArray[A](implicit @unused ev: Repr[A]): Repr[Array[A]] = new Repr[Array[A]] {
    def toSourceString(x: Array[A]): String = iteratorToSourceString(x.iterator, "Array")
  }

  implicit def reprForVector[A](implicit @unused ev: Repr[A]): Repr[Vector[A]] =
    reprForCollection[A, Vector]

  implicit def reprForOption[A](implicit @unused ev: Repr[A]): Repr[Option[A]] =
    new Repr[Option[A]] {
      def toSourceString(x: Option[A]): String =
        x match {
          case None    => "None"
          case Some(_) => iteratorToSourceString(x.iterator, "Some")
        }
    }

  implicit def reprForEither[L, R](implicit
      @unused evL: Repr[L],
      @unused evR: Repr[R]
  ): Repr[Either[L, R]] = new Repr[Either[L, R]] {
    def toSourceString(x: Either[L, R]): String =
      x match {
        case Left(err)    => iteratorToSourceString(Iterator(err), "Left")
        case Right(value) => iteratorToSourceString(Iterator(value), "Right")
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
        iteratorToSourceString(x.iterator, MultiLineRepr.collectionClassName(x))
    }

  private def iteratorToSourceString[A](x: Iterator[A], className: String)(implicit
      ev: Repr[A]
  ): String = {
    val out = new StringBuilder()
    MultiLineRepr.printApply[A](
      className,
      x,
      out
    )(value => out.append(ev.toSourceString(value)))
    out.toString
  }

  // Creates Repr instance based on pprint
  @deprecated(
    "This method is no longer recommended and will be removed in future releases. Use `default` instead.",
    "0.3.0"
  )
  def fromPprint[A]: Repr[A] = (a: A) =>
    // width and height are overridden to handle very large snapshots
    pprint.apply(a, width = 200, height = 99999999).plainText

  def default[A]: Repr[A] = MultiLineRepr.repr[A]

}
