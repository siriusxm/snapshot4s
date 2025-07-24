import snapshot4s.BuildInfo.snapshot4sVersion

val Scala3 = "3.3.0"
ThisBuild / crossScalaVersions := Seq("2.13.11", Scala3)
ThisBuild / scalaVersion       := Scala3
ThisBuild / tlBaseVersion      := "0.4"
lazy val root = tlCrossRootProject.aggregate(core, utils)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name                        := "core",
    snapshot4sResourceDirectory := CrossType.Pure
      .sharedResourcesDir(baseDirectory.value, "test")
      .get / "snapshot",
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies += "com.siriusxm" %%% "snapshot4s-weaver" % snapshot4sVersion % Test
  )
  .enablePlugins(Snapshot4sPlugin)

lazy val utils = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in(file("utils"))
  .settings(
    name                        := "utils",
    snapshot4sResourceDirectory := CrossType.Full
      .sharedResourcesDir(baseDirectory.value, "test")
      .get / "snapshot",
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    libraryDependencies += "com.siriusxm" %%% "snapshot4s-weaver" % snapshot4sVersion % Test
  )
  .enablePlugins(Snapshot4sPlugin)

lazy val framework = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full)
  .in(file("framework"))
  .settings(
    name := "framework",
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    libraryDependencies += "com.siriusxm" %%% "snapshot4s-weaver" % snapshot4sVersion % Test
  )
  .enablePlugins(Snapshot4sPlugin)
