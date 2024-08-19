---
sidebar_position: 5
---

# Inline snapshots

`assertInlineSnapshot` controls data defined in your source code.

You can use it to update values, such as case classes or sealed traits.

```scala mdoc:invisible
import snapshot4s.weaver.SnapshotExpectations.*
import snapshot4s.generated.snapshotConfig
```

```scala mdoc:silent
case class Config(environment: String, region: String)
val found = Config(environment = "dev", region = "us-east-2")

assertInlineSnapshot(found, Config(environment = "prod", region = "us-east-1"))
```

After running `sbt test` and `snapshot4sPromote`, the data will be updated.

```scala mdoc:nest:silent
case class Config(environment: String, region: String)
val found = Config(environment = "dev", region = "us-east-2")

assertInlineSnapshot(found, Config(environment = "dev", region = "us-east-2"))
```

You can also use it for strings. 

```scala mdoc:compile-only
assertInlineSnapshot(found = "The answer", "The answer")
```

If you have a long string, consider using [file snapshots](file-snapshots.md) instead.

## New snapshots

Use `???` to create a new snapshot:


```scala mdoc:nest:silent
case class Config(environment: String, region: String)
val found = Config(environment = "dev", region = "us-east-2")

assertInlineSnapshot(found, ???)
```

Promote your snapshots and see snapshot4s fill in the blanks:
```
sbt test
sbt snapshot4sPromote
```

```scala mdoc:nest:silent
case class Config(environment: String, region: String)
val found = Config(environment = "dev", region = "us-east-2")

assertInlineSnapshot(found, Config(environment = "dev", region = "us-east-2"))
```

## Supported data types

The `assertInlineSnapshot` assertion will work for products and case classes as well as strings and primitive types.

```scala mdoc:silent
assertInlineSnapshot(found = 1, 1)
assertInlineSnapshot(found = "The answer", "The answer")

case class Person(name: String)
assertInlineSnapshot(found = Person("Alice"), Person("Alice"))
assertInlineSnapshot(found = Nil, List(Person("Alice"), Person("Bob")))
```

## Unsupported data types

It should not be used on variables.

```scala mdoc:compile-only
val personVariable = Person("Bob") 
assertInlineSnapshot(found = Person("Alice"), personVariable) // Bad: personVariable will be replaced with Person("Alice")
assertInlineSnapshot(found = Person("Alice"), Person("Bob"))  // Good: "Bob" will be replaced with "Alice".
```

It fails to compile for values that can't be represented as source code.

```scala mdoc:fail
assertInlineSnapshot(found = new Object(), new Object())
```

