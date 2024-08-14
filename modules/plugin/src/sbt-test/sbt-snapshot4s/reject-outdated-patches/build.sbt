import snapshot4s.BuildInfo.snapshot4sVersion

lazy val root = (project in file("."))
  .settings(
    name                                  := "reject-outdated-patches",
    scalaVersion                          := "3.3.1",
    libraryDependencies += "com.siriusxm" %% "snapshot4s-weaver" % snapshot4sVersion % Test
  )
  .enablePlugins(Snapshot4sPlugin)
