---
sidebar_position: 7
---

# Known limitations

This page is a shortlist of the known limitations of snapshot4s. If you encounter any issues not listed below, we encourage you to [report them](https://github.com/siriusxm/snapshot4s/issues/new?template=bug.md).

## Scala Native is not supported

`snapshot4s` doesn't yet support Scala Native.

## `sbt-crossproject` is not supported

`snapshot4s` doesn't yet support the `CrossType.Pure` project layout of `sbt-crossproject`. This means that projects using `sbt-typelevel` are also not supported.

It should support all other project layouts, such as `sbt-projectmatrix`.

Support for `sbt-crossproject` is [on our roadmap](https://github.com/siriusxm/snapshot4s/issues/28).

## Promoted code may fail to compile with `not found` errors

`snapshot4s` uses `pprint` to efficiently generate the source code corresponding to an expected value.  This doesn’t add import statements for `enum` and classes within objects. You may need to add an import statement corresponding to the `enum` or object in question.

## Promoted code isn’t formatted

`snapshot4s` doesn't depend on a formatter, so can’t format its generated code. If you'd like to run `scalafmt` automatically after promoting your snapshots, you can redefine `snapshot4sPromote` in your `build.sbt`:

```scala
val root = (project in file("."))
  .settings(
    snapshot4sPromote := {
      snapshot4sPromote.value
      (Test / scalafmt).value
    })
  .enablePlugins(Snapshot4sPlugin)
```
