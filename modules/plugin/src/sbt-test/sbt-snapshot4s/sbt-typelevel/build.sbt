import snapshot4s.BuildInfo.snapshot4sVersion

// lazy val root = (project in file("."))
//   .settings(
//     scalaVersion := "3.3.1",
//     Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
//     testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
//     libraryDependencies += "com.siriusxm" %%% "snapshot4s-weaver" % snapshot4sVersion % Test
//   )
//   .enablePlugins(Snapshot4sPlugin, ScalaJSPlugin)
  
val Scala3 = "3.3.0"
ThisBuild / crossScalaVersions := Seq("2.13.11", Scala3)
ThisBuild / scalaVersion := Scala3 // the default Scala
ThisBuild / tlBaseVersion := "0.4" // your current series x.y

lazy val root = tlCrossRootProject.aggregate(core)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "woozle-core",
    description := "Core data types and typeclasses",
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    libraryDependencies += "com.siriusxm" %%% "snapshot4s-weaver" % snapshot4sVersion % Test
  ).enablePlugins(Snapshot4sPlugin)
