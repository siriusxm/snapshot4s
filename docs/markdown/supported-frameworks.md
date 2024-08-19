---
sidebar_position: 4
---
# Supported frameworks

`snapshot4s` comes with out of the box integrations for:
 - Weaver via `snapshot4s-weaver`
 - MUnit via `snapshot4s-munit`
 - ScalaTest via `snapshot4s-scalatest`

All integrations support Scala.js.

If you use a different test framework, you can easily [develop your own integration](contributing/supporting-a-test-framework.md).

## Weaver

Add the `snapshot4s-weaver` dependency to your `build.sbt`.

```scala
import snapshot4s.BuildInfo.snapshot4sVersion

libraryDependencies += "com.siriusxm" %% "snapshot4s-weaver" % snapshot4sVersion % Test
```

For Scala.js, use `%%%` and [emit a module][Scala.js modules].
```scala
import snapshot4s.BuildInfo.snapshot4sVersion

libraryDependencies += "com.siriusxm" %%% "snapshot4s-weaver" % snapshot4sVersion % Test
Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
```

Extend the `SnapshotExpectations` trait and import `snapshot4s.generated.*`.

```scala mdoc
import snapshot4s.weaver.SnapshotExpectations
import snapshot4s.generated.*

object MySuite extends weaver.SimpleIOSuite with SnapshotExpectations {
  test("snapshot4s can fill in the blanks") {
    val mySnapshotWorkflow = "snapshot4s"
    assertInlineSnapshot(mySnapshotWorkflow, ???)
  }
  test("snapshot4s can update code") {
    val mySnapshotCode = List(1, 2, 3)
    assertInlineSnapshot(mySnapshotCode, Nil)
  }
  test("snapshot4s can work with files") {
    val mySnapshotWorkflow = "snapshot4s"
    assertFileSnapshot(mySnapshotWorkflow, "mySnapshotWorkflow")
  }
}
```

## Munit
Add the `snapshot4s-munit` dependency to your `build.sbt`.

```scala
import snapshot4s.BuildInfo.snapshot4sVersion

libraryDependencies += "com.siriusxm" %% "snapshot4s-munit" % snapshot4sVersion % Test
```

For Scala.js, use `%%%` and [emit a module][Scala.js modules].
```scala
import snapshot4s.BuildInfo.snapshot4sVersion

libraryDependencies += "com.siriusxm" %%% "snapshot4s-munit" % snapshot4sVersion % Test
Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
```

Extend the `SnapshotAssertions` trait and import `snapshot4s.generated.*`.

```scala mdoc:invisible:reset
// The munit location macro can't calculate the source location of the md file.
// This implicit circumvents it.
import munit.Location
implicit val sourceloc: Location = Location.empty
```

```scala mdoc
import snapshot4s.munit.SnapshotAssertions
import snapshot4s.generated.*

object MySuite extends munit.FunSuite with SnapshotAssertions {
  test("snapshot4s can fill in the blanks") {
    val mySnapshotWorkflow = "snapshot4s"
    assertInlineSnapshot(mySnapshotWorkflow, ???)
  }
  test("snapshot4s can update code") {
    val mySnapshotCode = List(1, 2, 3)
    assertInlineSnapshot(mySnapshotCode, Nil)
  }
  test("snapshot4s can work with files") {
    val mySnapshotWorkflow = "snapshot4s"
    assertFileSnapshot(mySnapshotWorkflow, "mySnapshotWorkflow")
  }
}
```

## ScalaTest
Add the `snapshot4s-scalatest` dependency to your `build.sbt`.

```scala
import snapshot4s.BuildInfo.snapshot4sVersion

libraryDependencies += "com.siriusxm" %% "snapshot4s-scalatest" % snapshot4sVersion % Test
```

For Scala.js, use `%%%` and [emit a module][Scala.js modules].
```scala
import snapshot4s.BuildInfo.snapshot4sVersion

libraryDependencies += "com.siriusxm" %%% "snapshot4s-scalatest" % snapshot4sVersion % Test
Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
```

Extend the `SnapshotAssertions` trait and import `snapshot4s.generated.*`.

```scala mdoc:reset
import org.scalatest.flatspec.AnyFlatSpec
import snapshot4s.scalatest.SnapshotAssertions
import snapshot4s.generated.*

class MySuite extends AnyFlatSpec with SnapshotAssertions {
  "snapshot4s" should "fill in the blanks" in {
    val mySnapshotWorkflow = "snapshot4s"
    assertInlineSnapshot(mySnapshotWorkflow, ???)
  }
  it should "update code" in {
    val mySnapshotCode = List(1, 2, 3)
    assertInlineSnapshot(mySnapshotCode, Nil)
  }
  it should "work with files" in {
    val mySnapshotWorkflow = "snapshot4s"
    assertFileSnapshot(mySnapshotWorkflow, "mySnapshotWorkflow")
  }
}
```

[Scala.js modules]: https://www.scala-js.org/doc/project/module.html
