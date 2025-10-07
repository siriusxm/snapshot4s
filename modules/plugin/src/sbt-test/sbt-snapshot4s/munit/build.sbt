import snapshot4s.BuildInfo.snapshot4sVersion

lazy val root = (project in file("."))
  .settings(
    scalaVersion       := "3.3.1",
    crossScalaVersions := Seq("3.3.1", "2.12.20", "2.13.17"),
    scalacOptions += "-Xsource:3",
    libraryDependencies += "com.siriusxm" %% "snapshot4s-munit" % snapshot4sVersion % Test
  )
  .enablePlugins(Snapshot4sPlugin)
