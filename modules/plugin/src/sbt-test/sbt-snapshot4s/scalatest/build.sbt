import snapshot4s.BuildInfo.snapshot4sVersion

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "3.3.1",
    scalacOptions += "-Xsource:3",
    crossScalaVersions := Seq("3.3.1", "2.12.20", "2.13.14"),
    libraryDependencies ++= Seq(
      "com.siriusxm"  %% "snapshot4s-scalatest" % snapshot4sVersion % Test,
      "org.scalatest" %% "scalatest"            % "3.2.19"          % Test
    )
  )
  .enablePlugins(Snapshot4sPlugin)
