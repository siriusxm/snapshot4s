---
sidebar_position: 5
---

# File snapshots

Snapshots can be stored in files in the `test/resources/snapshot` directory.

`assertFileSnapshot` tests that a string matches the contents of a snapshot file.

To use a snapshot, pass in a relative path to `assertFileSnapshot`.

For example, to use a snapshot in `test/resources/snapshot/config.json`:

```scala mdoc:invisible
import snapshot4s.weaver.SnapshotExpectations.*
import snapshot4s.generated.snapshotConfig
```

```scala mdoc:compile-only
val found = """{"environment": "dev", "region": "us-east-2"}"""
assertFileSnapshot(found, "config.json")
```

## New snapshots

You can create a new snapshot file by passing the relative path to `assertFileSnapshot`.

```scala mdoc:compile-only
val found = """{"environment": "dev", "region": "us-east-2"}"""
assertFileSnapshot(found, "new-config.json")
```

Run your tests and promote your snapshots:
```
sbt test
sbt snapshot4sPromote
```

Your snapshot will be created in `test/resources/snapshot/new-config.json`:

## Supported datatypes

The `assertFileSnapshot` assertion only supports strings.

If you'd like to test a non-String value, consider writing it in code using [inline snapshots](inline-snapshots.md).
