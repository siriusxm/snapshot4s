---
sidebar_position: 8
title: Custom types support
---


# Custom types support with `Repr`

Snapshot4s serializes the runtime values to it's source code representation. It does that by utilizing the [`Repr` typeclass](https://github.com/siriusxm/snapshot4s/blob/main/modules/core/src/main/scala/com/siriusxm/snapshot4s/Repr.scala). The library is able to automatically derive the implementation for primitive types, collections from standard library along with `Option` and `Either`. As for now it's implemented using [PPrint](https://github.com/com-lihaoyi/PPrint/) but that might change in future.

In case the `Repr` instance is missing for your type `MyType`, you'll see a message like this:

:::warning[Compiler error]
Could not find implicit instance for `Repr[MyType]`. This usually means that `MyType` or it's component is not an ADT or primitive type. In that case provide your own given instance of `Repr[MyType]`. Repr.fromPprint can be used to generate it.
:::

If you want to assert snapshot against a type `MyType`, for which the library doesn't derive the implementation, you can implement your own instance of `Repr[MyType]`. Make sure to mark to expose it in implicit scope (with `implicit val` in Scala 2 or `given` in Scala 3).

## Defining your own `Repr`

There are two ways of implementing `Repr`. If your type `MyType` serializes to source code properly with `PPrint`, you can simply do `given Repr[MyType] = Repr.fromPprint[MyType]`. While convenient, this method does not always produce satisfying results. For example types that come from Java will not serialize nicely. In that case you can define `Repr` as follows:


```scala mdoc:invisible:reset
// The munit location macro can't calculate the source location of the md file.
// This implicit circumvents it.
import munit.Location
implicit val sourceloc: Location = Location.empty
```

```scala mdoc:invisible
import snapshot4s.munit.SnapshotAssertions
import snapshot4s.generated.snapshotConfig
object CustomReprSuite extends munit.FunSuite with SnapshotAssertions
import CustomReprSuite.*
```

```scala mdoc
import java.util.UUID
import snapshot4s.Repr

case class User(name: String, id: UUID)
def getUsers(): List[User] = ???

// here we define custom Repr implementation for UUID, using Single Abstract Method syntax
given Repr[UUID] = (uuid: UUID) => s"""UUID.fromString("${uuid.toString}")"""

test("should get users") {
  val users = getUsers()
  assertInlineSnapshot(
    users,
    List(
      User(name = "admin", id = UUID.fromString("6e09d8b0-b7c9-4cbe-ac96-8c27c36cd2c6")),
      User(name = "alice", id = UUID.fromString("ff8b8542-183f-4eff-acb0-6436936ccc31")),
      User(name = "bob", id = UUID.fromString("30046607-623d-4934-b8f6-4002ef4ff588"))
    )
  )
  //                           ☝️ notice how UUID representation follows our Repr implementation
}
```
