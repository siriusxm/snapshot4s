---
sidebar_position: 3
---

# Setting up your project

You can enable `snapshot4s` for a variety of project layouts.

 - For single project SBT builds follow [these instructions](#single-project-builds).
 - For multi-project SBT builds follow [these instructions](#multi-project-builds)
 - For `sbt-projectmatrix` follow [these instructions](#sbt-projectmatrix).
 - For `sbt-crossproject` and `sbt-typelevel` follow [these instructions](#sbt-crossproject-and-sbt-typelevel).
 - For `mill` builds follow [these instructions](#mill-builds).

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

## Mill builds

Snapshot4s supports [mill](https://mill-build.org/mill/index.html) version 1.

Import the `mill-snapshot4s` plugin into your `build.mill`.

```scala
//| mvnDeps: ["com.siriusxm::mill-snapshot4s::@LATEST_STABLE_VERSION@"]
```

Extend the `Snapshot4sModule` in your `test` object. 

```scala
import snapshot4s.Snapshot4sModule

object myProject extends ScalaModule {
  object test extends ScalaTests with Snapshot4sModule { ... }
}
```

Finally, add the integration library for your [test framework](./supported-frameworks.md).

For example, to use `snapshot4s` with `MUnit`:

```scala
//| mvnDeps: ["com.siriusxm::mill-snapshot4s::@LATEST_STABLE_VERSION@"]
import snapshot4s.Snapshot4sModule

object myProject extends ScalaModule {
  object test extends ScalaTests with Snapshot4sModule with TestModule.Munit {
    override def mvnDeps =
      super.mvnDeps() :+ mvn"com.siriusxm::snapshot4s-munit:${snapshot4s.BuildInfo.snapshot4sVersion}"
  }
}
```

You can update your tests via the `snapshot4sPromote` task:

```sh
mill myProject.test.snapshot4sPromote
```
