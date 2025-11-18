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

  implicit val reprForString: Repr[String] = fromPprint
  implicit val reprForChar: Repr[Char]     = fromPprint
  implicit val reprForUnit: Repr[Unit]     = fromPprint

  implicit val reprForBoolean: Repr[Boolean] = fromPprint
  implicit val reprForShort: Repr[Short]     = fromPprint
  implicit val reprForInt: Repr[Int]         = fromPprint
  implicit val reprForLong: Repr[Long]       = fromPprint
  implicit val reprForFloat: Repr[Float]     = fromPprint
  implicit val reprForDouble: Repr[Double]   = fromPprint

  implicit def reprForIterable[A](implicit @unused ev: Repr[A]): Repr[Iterable[A]] = fromPprint
  implicit def reprForSeq[A](implicit @unused ev: Repr[A]): Repr[Seq[A]]           = fromPprint
  implicit def reprForList[A](implicit @unused ev: Repr[A]): Repr[List[A]]         = fromPprint
  implicit def reprForArray[A](implicit @unused ev: Repr[A]): Repr[Array[A]]       = fromPprint
  implicit def reprForVector[A](implicit @unused ev: Repr[A]): Repr[Vector[A]]     = fromPprint
  implicit def reprForOption[A](implicit @unused ev: Repr[A]): Repr[Option[A]]     = fromPprint

  implicit def reprForEither[L, R](implicit
      @unused evL: Repr[L],
      @unused evR: Repr[R]
  ): Repr[Either[L, R]] = fromPprint

  // Creates Repr instance based on pprint
  def fromPprint[A]: Repr[A] = (a: A) =>
    // width and height are overridden to handle very large snapshots
    pprint.apply(a, width = 200, height = 99999999).plainText

  def default[A]: Repr[A] = MultiLineRepr.repr[A]

}
