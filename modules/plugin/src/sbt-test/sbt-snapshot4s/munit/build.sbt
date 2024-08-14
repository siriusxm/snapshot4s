import snapshot4s.BuildInfo.snapshot4sVersion

lazy val root = (project in file("."))
  .settings(
    name                                  := "munit",
    scalaVersion                          := "3.3.1",
    crossScalaVersions                    := Seq("3.3.1", "2.12.19", "2.13.14"),
    libraryDependencies += "com.siriusxm" %% "snapshot4s-munit" % snapshot4sVersion % Test
  )
  .enablePlugins(Snapshot4sPlugin)
