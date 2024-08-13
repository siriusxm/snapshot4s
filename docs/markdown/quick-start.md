---
sidebar_position: 2
---

# Quick start

Add the plugin to `plugins.sbt`.

```scala
addSbtPlugin("com.siriusxm" % "sbt-snapshot4s" % "@LATEST_STABLE_VERSION@")
```

Enable it in `build.sbt`.

```scala
val root = (project in file(".")).enablePlugins(Snapshot4sPlugin)
```

Add the integration library for your test framework. See [supported frameworks](supported-frameworks.md) for more.

```scala
import snapshot4s.BuildInfo.snapshot4sVersion

libraryDependencies += "com.siriusxm" %% "snapshot4s-munit" % snapshot4sVersion % Test
```

Write a test.

```scala mdoc:invisible:reset
// The munit location macro can't calculate the source location of the md file.
// This implicit circumvents it.
import munit.Location
implicit val sourceloc: Location = Location.empty
```

```scala mdoc
import snapshot4s.munit.SnapshotAssertions
import snapshot4s.generated.snapshotConfig

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

Run `test` and watch your tests fail.
Run `snapshot4sPromote` and be bedazzled. You should now see:
```scala mdoc:invisible
import MySuite.*
```
```scala mdoc
test("snapshot4s can update code") {
  val mySnapshotCode = List(1, 2, 3)
  assertInlineSnapshot(mySnapshotCode, List(1, 2, 3)) // <- spot the difference
}
```

as well as a new `mySnapshotWorkflow` file.

Run `test` again and watch them pass.
