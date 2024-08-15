import snapshot4s.BuildInfo.snapshot4sVersion

// Snapshot4s works correctly.
// The core Snapshot4sBuildInfo is on the classpath for tests
lazy val core = (project in file("core"))
  .settings(
    scalaVersion                          := "3.3.1",
    libraryDependencies += "com.siriusxm" %% "snapshot4s-weaver" % snapshot4sVersion % Test
  )
  .enablePlugins(Snapshot4sPlugin)

// Snapshot4s works correctly.
// The utils Snapshot4sBuildInfo is on the classpath for tests
// The core Snapshot4sBuildInfo is not on the Test classpath because utils / Test doesn't depend on core / Test
lazy val utils = (project in file("utils"))
  .settings(
    scalaVersion                          := "3.3.1",
    libraryDependencies += "com.siriusxm" %% "snapshot4s-weaver" % snapshot4sVersion % Test
  ).dependsOn(core)
  .enablePlugins(Snapshot4sPlugin)

// Snapshot4s works correctly, but the setup is odd.
// The framework Snapshot4sBuildInfo is on the classpath for tests
// The core Snapshot4sBuildInfo isalso on the Test classpath because framework / Test depends on core / Test
//   but the framework Snapshot4sBuildInfo takes precedence over it.
lazy val framework = (project in file("framework"))
  .settings(
    scalaVersion                          := "3.3.1",
    libraryDependencies += "com.siriusxm" %% "snapshot4s-weaver" % snapshot4sVersion % Test
  ).dependsOn(core % "compile->compile;test->test")
  .enablePlugins(Snapshot4sPlugin)

// Snapshot4s is broken! It updates the wrong tests.
// Note that we forgot to enable the Snapshot4sPlugin, so extra Snapshot4sBuildInfo is not generated.
// Inline snapshots aren't written because they have some validation on paths.
// File snapshot patches don't have this, so are written to the core target directory.
lazy val extra = (project in file("extra"))
  .settings(
    scalaVersion                          := "3.3.1",
    libraryDependencies += "com.siriusxm" %% "snapshot4s-weaver" % snapshot4sVersion % Test
  ).dependsOn(core % "compile->compile;test->test")

lazy val root = (project in file("."))
  .aggregate(core, utils, framework, extra)
