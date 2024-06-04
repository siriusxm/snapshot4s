import snapshot4s.BuildInfo.snapshot4sVersion

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "3.3.1",
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    libraryDependencies += "com.siriusxm" %%% "snapshot4s-weaver" % snapshot4sVersion % Test
  )
  .enablePlugins(Snapshot4sPlugin, ScalaJSPlugin)
