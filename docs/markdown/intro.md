---
sidebar_label: Introduction
sidebar_position: 1
---

# What is it?

`snapshot4s` is a tool that updates your tests on command.

You can use it with `munit` and `weaver`. It supports tests in Scala 2, Scala 3 and Scala.js.

A snapshot test is a unit test that asserts that a found value is equal to a previously snapshotted value.

```scala mdoc:invisible:reset
// The munit location macro can't calculate the source location of the md file.
// This implicit circumvents it.
import munit.Location
implicit val sourceloc: Location = Location.empty
```

```scala mdoc:invisible
import snapshot4s.munit.SnapshotAssertions
import snapshot4s.generated.snapshotConfig
object MySuite extends munit.FunSuite with SnapshotAssertions
import MySuite.*

case class Pet(name: String, age: Int)
def getPets(): List[Pet] = ???
```

```scala mdoc
test("should get all pets") {
  val pets = getPets()
  assertInlineSnapshot(
    pets,
    List(
      Pet(name = "Mistoffelees", age = 6),
      Pet(name = "Grizabella", age = 14),
      Pet(name = "Bombalurina", age = 5)
    )
  )
}
```

`assertInlineSnapshot` is like `assert` with superpowers. It takes control of its expected value and updates it on command.

## Why use it?

It'll save you a ton of time and effort when updating your tests.

Suppose we intentionally change `getPets`: with our new logic, all pets are one year older.

We would have to update our test with the new ages:

```scala mdoc
test("should get all pets") {
  val pets = getPets()
  assertInlineSnapshot(
    pets,
    List(
      Pet(name = "Mistoffelees", age = 7),
      Pet(name = "Grizabella", age = 15),
      Pet(name = "Bombalurina", age = 6)
    )
  )
}
```

This is easy enough to do manually for one single test. It quickly becomes boring when there are many, especially when the expected values are large.

`snapshot4s` updates all your tests for you with a single sbt command.

Following the test failure, we can run `snapshot4sPromote` to replace the expected value with the actual value.

```sh
sbt test                  # Fails as snapshots do not match.
sbt snapshot4sPromote     # Updates snapshots
sbt test                  # Succeeds!
```

Instead of updating the tests manually yourself, you only need to review them using your favourite difftool.
