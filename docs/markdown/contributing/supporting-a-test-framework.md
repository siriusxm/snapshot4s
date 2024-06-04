---
sidebar_position: 2
---
# Supporting a test framework

## Supported test frameworks

`snapshot4s` comes with out of the box integrations for:
 - `weaver` via `snapshot4s-weaver`
 - `munit` via `snapshot4s-munit`

If you use a different test framework, you can easily develop your own integration.

## Integrating your own test framework

Decide on the `Result` type of your integration. This is how your test framework represents the results of its `assert` statements. For example:
 - `munit` executes the assertion and throws exceptions on failure. It has the result type of `Unit`.
 - `weaver` suspends the assertion in `IO[Expectations]`.

Define a trait extending `SnapshotAssertions[Result]`.

Derive a `ResultLike` typeclass instance to lift an `snapshot4s` result into a framework-specific result. You should execute the `result` function and decide what to do on failure, success, and on the lack of an existing snapshot.

Finally, derive a `SnapshotEq` typeclass instance to define equality for your test framework. 

```scala mdoc
import snapshot4s.{SnapshotAssertions, ResultLike, SnapshotEq, Result}

// This framework throws exceptions on failure, so its result type is `Unit`.
trait CustomAssertions extends SnapshotAssertions[Unit] {

   // Derive a `ResultLike` instance.
   implicit def customResultLike[A]: ResultLike[A, Unit] = new ResultLike[A, Unit] {
      def apply(result: () => Result[A]): Unit = result() match {
	     case Result.NonExistent(_) => throw new Error("Snapshot needs to be generated!")
	     case Result.Failure(found, expected) => throw new Error(s"$found was not equal to $expected!")
	     case Result.Success(_, _) => ()
	  }
   }
   
   // Derive a `SnapshotEq` instance.
   implicit def customSnapshotEq[A]: SnapshotEq[A] = SnapshotEq.fromUniversalEquals[A]
}
```

As an example, have a look at the integrations for [weaver](https://github.com/siriusxm/snapshot4s/tree/main/modules/weaver) and [munit](https://github.com/siriusxm/snapshot4s/tree/main/modules/munit).

Your users can then extend `CustomAssertions` in their own test suites:

```scala mdoc:compile-only
import snapshot4s.generated.snapshotConfig

class MySuite extends CustomAssertions {
    assertInlineSnapshot("found", ???)
}
```
