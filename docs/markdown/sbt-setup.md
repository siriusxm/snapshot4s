---
sidebar_position: 3
---

# SBT setup

You can enable `snapshot4s` for a variety of SBT project layouts.

 - For single project SBT builds follow [these instructions](#single-project-builds).
 - For multi-project SBT builds follow [these instructions](#multi-project-builds)
 - For `sbt-projectmatrix` follow [these instructions](#sbt-projectmatrix).
 - For `sbt-crossproject` and `sbt-typelevel` follow [these instructions](#sbt-crossproject-and-sbt-typelevel).

## Single project builds

For single project builds, follow the [quick start](./quick-start.md#quick-start).

## Multi-project builds

If you have a multi-project setup, you must enable the plugin in each project containing snapshot tests.

Add the plugin to `plugins.sbt`.

```scala
addSbtPlugin("com.siriusxm" % "sbt-snapshot4s" % "@LATEST_STABLE_VERSION@")
```

Enable it for each project in `build.sbt`.

```scala
val core = (project in file("core")).enablePlugins(Snapshot4sPlugin)

val utils = (project in file("utils")).enablePlugins(Snapshot4sPlugin)

val root = (project in file(".")).aggregate(core, utils)
```

Finally, add the integration library for your [test framework](./supported-frameworks.md).

## sbt-projectmatrix

If you use `sbt-projectmatrix`, you can enable the plugin for a matrix.

Add the plugin to `plugins.sbt`.

```scala
addSbtPlugin("com.siriusxm" % "sbt-snapshot4s" % "@LATEST_STABLE_VERSION@")
```

Enable it for each matrix in `build.sbt`.

```scala
val core = (projectMatrix in file("core")).enablePlugins(Snapshot4sPlugin)
```

Finally, add the integration library for your [test framework](./supported-frameworks.md).

## sbt-crossproject and sbt-typelevel

You can enable the plugin for all cross types, for both JVM and JS.

Add the plugin to `plugins.sbt`.

```scala
addSbtPlugin("com.siriusxm" % "sbt-snapshot4s" % "@LATEST_STABLE_VERSION@")
```

Enable the plugin.

```scala
val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Dummy)
  .in(file("core"))
  .enablePlugins(Snapshot4sPlugin)
```

If you use the `Pure` or `Full` cross types, set the `snapshot4sResourceDirectory` to the shared test resource directory.

```scala
val core = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    snapshot4sResourceDirectory := CrossType.Pure
      .sharedResourcesDir(baseDirectory.value, "test")
      .get / "snapshot",
  )
  .enablePlugins(Snapshot4sPlugin)
```

Finally, add the integration library for your [test framework](./supported-frameworks.md).
