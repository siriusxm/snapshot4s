import snapshot4s.BuildInfo.snapshot4sVersion

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "3.3.7",
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    libraryDependencies += "com.siriusxm" %%% "snapshot4s-weaver" % snapshot4sVersion % Test
  )
  .enablePlugins(Snapshot4sPlugin, ScalaNativePlugin)
