import snapshot4s.BuildInfo.snapshot4sVersion

lazy val scala3Version   = "3.4.2"
lazy val scala213Version = "2.13.18"
lazy val scala212Version = "2.12.21"
lazy val scalaVersions   = Seq(scala3Version, scala213Version, scala212Version)

lazy val core = (projectMatrix in file("core"))
  .settings(
    scalacOptions += "-Xsource:3",
    libraryDependencies += "com.siriusxm" %%% "snapshot4s-weaver" % snapshot4sVersion % Test
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .jsPlatform(
    scalaVersions = scalaVersions,
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .enablePlugins(Snapshot4sPlugin)

lazy val root = (project in file("."))
  .aggregate((core.projectRefs): _*)
