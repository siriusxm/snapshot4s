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

import cats.syntax.all.*
import org.scalacheck.Gen
import weaver.*
import weaver.scalacheck.*

object HashingSpec extends SimpleIOSuite with Checkers {

  val hashGen      = Gen.hexStr
  val patchFileGen = Gen.stringOf(Gen.alphaChar)

  val hashAndFileGen = for {
    hash       <- hashGen
    restOfFile <- patchFileGen
  } yield (hash, restOfFile)

  test("produces reproducible hash") {
    forall { (input: String) =>
      val firstHash  = Hashing.calculateHash(input)
      val secondHash = Hashing.calculateHash(input)
      expect.eql(firstHash, secondHash)
    }
  }

  test("produces hash header") {
    val hash   = "1234"
    val header = Hashing.produceHashHeader(hash)
    assert(header.startsWith("# Hash: 1234")).pure
  }

  test("extracts hash when possible") {
    forall(hashGen) { (hash: String) =>
      val header    = Hashing.produceHashHeader(hash)
      val maybeHash = Hashing.extractHash(header)
      expect.eql(maybeHash, Some(hash))
    }
  }

  test("verifyAndRemoveHash returns patch content without hash when valid hash provided") {
    forall(hashAndFileGen) { case (hash: String, restOfFile: String) =>
      val header               = Hashing.produceHashHeader(hash)
      val contents             = s"$header\n$restOfFile"
      val contentWithoutHeader = Hashing.verifyAndRemoveHash(hash)(contents)
      expect.eql(contentWithoutHeader, Some(restOfFile))
    }
  }

  test("verifyAndRemoveHash returns None when hash don't match") {
    forall(hashAndFileGen) { case (hash: String, restOfFile: String) =>
      val invalidHash          = "1234567890"
      val header               = Hashing.produceHashHeader(invalidHash)
      val contents             = s"$header\n$restOfFile"
      val contentWithoutHeader = Hashing.verifyAndRemoveHash(hash)(contents)
      expect.eql(contentWithoutHeader, None)
    }
  }

}
