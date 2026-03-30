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

import cats.effect.{IO, Resource}
import mill.*
import mill.api.Discover
import mill.javalib.*
import mill.scalalib.*
import mill.testkit.{TestRootModule, UnitTester}
import weaver.*

object Snapshot4sPromoteSpec extends SimpleIOSuite {

  // Mill's unit tester relies on a global classloader, so can only run one test at a time.
  override def maxParallelism: Int = 1

  private trait Build extends TestRootModule with ScalaModule {
    def scalaVersion = "3.8.2"

    lazy val millDiscover = Discover[this.type]

    object test extends ScalaTests with Snapshot4sModule with TestModule.Weaver {
      override def mvnDeps =
        Seq(mvn"com.siriusxm::snapshot4s-weaver:${BuildInfo.snapshot4sVersion}")
    }
  }

  private def evaluate[A](f: (UnitTester, Build) => A): IO[A] = {
    object build extends Build
    IO {
      val tempDir = os.temp.dir()
      os.copy.over(os.Path(BuildInfo.testResourceDirectory) / "simple-test", tempDir)
      tempDir
    }.flatMap { tempDir =>
      Resource
        .fromAutoCloseable(IO(UnitTester(build, tempDir)))
        .use { tester =>
          IO(tester.scoped(eval => f(eval, build)))
        }
    }
  }

  test("compilation succeeds") {
    evaluate((eval, build) => eval(build.test.compile))
      .map { result => expect(clue(result).isRight) }
  }

  test("promote succeeds when no snapshots are present") {
    evaluate((eval, build) => eval(build.test.snapshot4sPromote()))
      .map { result => expect(clue(result).isRight) }
  }

  test("promote successfully applies snapshots") {
    evaluate { (eval, build) =>
      val firstTestResult  = eval(build.test.testForked(""))
      val promoteResult    = eval(build.test.snapshot4sPromote())
      val secondTestResult = eval(build.test.testForked(""))
      (firstTestResult, promoteResult, secondTestResult)
    }.map { result =>
      val (firstTestResult, promoteResult, secondTestResult) = result
      expect(clue(firstTestResult).isLeft)
        .and(expect(clue(promoteResult).isRight))
        .and(expect(clue(secondTestResult).isRight))
    }
  }

  test("promote applies snapshots to specific files") {
    evaluate { (eval, build) =>
      val firstTestResult = eval(build.test.testForked(""))
      eval(build.test.snapshot4sPromote("*SimpleTest*"))
      val secondTestResult = eval(build.test.testForked(""))
      eval(build.test.snapshot4sPromote("*OtherTest*"))
      val thirdTestResult = eval(build.test.testForked(""))
      (firstTestResult, secondTestResult, thirdTestResult)
    }.map { result =>
      val (firstTestResult, secondTestResult, thirdTestResult) = result
      expect(clue(firstTestResult).isLeft)
        .and(expect(clue(secondTestResult).isLeft))
        .and(expect(clue(thirdTestResult).isRight))
    }
  }

}
