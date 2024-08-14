---
sidebar_position: 1
---

# How it works

The implementation consists of the `snapshot4s-core` and integration libraries, which contain the snapshot assertion functions, and the `Snapshot4sPlugin` in the `project` directory.

## Inline snapshots

In order to update an inline code snapshot, we need to know its source location. This can only be done at compile time through a macro.

The `assertInlineSnapshot` function is a macro. It takes two arguments, `found` and `snapshot`, where `snapshot` must be an inline value. It inspects the start and end position of `snapshot`, and calculates the path to the source file.

At runtime, the `AssertInlineSnapshotMacro.assertInlineSnapshot` function is called. This compares the `found` and `snapshot` values. If there is a difference, it stores the `found` value's string representation in a file and fails the expectation. This file is later used by `snapshot4sPromote` to update the `snapshot` value.

The metadata required to make the update - the source file and positions — are stored in the file path. For snapshot, if the file is `scala/com/siriusxm/testing/Snapshot4sSuite.scala` and the start and end positions are `1309` and `1320`, the file path is:

```
target/snapshot/inline-patch/scala/com/siriusxm/testing/Snapshot4sSuite.scala/1309-1320
```

The `snapshot4sPromote` task can then scan the `inline-patch` directory and determine the source file and positions from the file path alone.

## File-based snapshots

The `assertFileSnapshot` function is considerably easier to understand. It determines the expected content from the `resources/snapshot` directory, compares it to the `found` value. If there is a difference, the `found` value is stored in the `target/snapshot/resource-patch` directory, with the same file path as the resource. For example, if the resource was `resources/snapshot/SnapshotSuite`, the patch would be stored in:

```
target/snapshot/resource-patch/SnapshotSuite
```

The `snapshot4sPromote` task overwrites the original file.

## Config

The `SnapshotConfig` data structure contains various pieces of build info needed at runtime, such as the location of the `resource` and `target` directory. An implicit `SnapshotConfig` value is generated using a SBT source generator in the `Snapshot4sPlugin`. It can be imported with:

```scala
import snapshot4s.my_project.generated.snapshotConfig
```
