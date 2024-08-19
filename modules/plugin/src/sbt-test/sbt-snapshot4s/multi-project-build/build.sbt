import snapshot4s.BuildInfo.snapshot4sVersion

lazy val core = (project in file("core"))
  .settings(
    scalaVersion                          := "3.3.1",
    libraryDependencies += "com.siriusxm" %% "snapshot4s-weaver" % snapshot4sVersion % Test
  )
  .enablePlugins(Snapshot4sPlugin)

lazy val utils = (project in file("utils"))
  .settings(
    scalaVersion                          := "3.3.1",
    libraryDependencies += "com.siriusxm" %% "snapshot4s-weaver" % snapshot4sVersion % Test
  ).dependsOn(core)
  .enablePlugins(Snapshot4sPlugin)

lazy val framework = (project in file("framework"))
  .settings(
    scalaVersion                          := "3.3.1",
    libraryDependencies += "com.siriusxm" %% "snapshot4s-weaver" % snapshot4sVersion % Test
  ).dependsOn(core % "compile->compile;test->test")
  .enablePlugins(Snapshot4sPlugin)

lazy val root = (project in file("."))
  .aggregate(core, utils, framework)
