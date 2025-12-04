import com.typesafe.tools.mima.core.ProblemFilters
import com.typesafe.tools.mima.core.*
import org.typelevel.sbt.gha.RefPredicate
import org.typelevel.sbt.gha.Ref
import org.scalafmt.sbt.ScalafmtPlugin.{globalInstance => Scalafmt}

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val buildInfoSettings: Seq[Setting[?]] = {
  val infoKeys = Seq[BuildInfoKey](
    name,
    BuildInfoKey.map(version)(kv => ("snapshot4sVersion", kv._2)),
    scalaVersion,
    sbtVersion,
    git.gitCurrentBranch,
    git.gitHeadCommit
  )

  Seq(
    buildInfoPackage := "snapshot4s",
    buildInfoKeys    := infoKeys
  )
}

lazy val scala3Version         = "3.3.7"
lazy val scala213Version       = "2.13.18"
lazy val sbtPluginScalaVersion = "2.12.20"

lazy val scalaVersions = Seq(
  scala3Version,
  scala213Version,
  sbtPluginScalaVersion
)

def scalaReflect(scalaVersion: String): List[ModuleID] =
  if (scalaVersion.startsWith("3.")) Nil
  else List("org.scala-lang" % "scala-reflect" % scalaVersion)

inThisBuild(
  Seq(
    scalaVersion  := scala3Version,
    versionScheme := Some("early-semver"),
    organization  := "com.siriusxm",
    developers ++= List(
      tlGitHubDev("miciek", "Michał Płachta"),
      tlGitHubDev("majk-p", "Michał Pawlik"),
      tlGitHubDev("zainab-ali", "Zainab Ali")
    ),
    githubWorkflowOSes         := Seq("ubuntu-22.04", "windows-2025"),
    githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17")),
    githubWorkflowPublishTargetBranches += RefPredicate.Equals(Ref.Branch("main")),
    ThisBuild / githubWorkflowBuild += WorkflowStep
      .Run(List("sbt scripted"), name = Some("Scripted tests")),
    tlBaseVersion          := "0.2",
    tlUntaggedAreSnapshots := false,
    tlFatalWarnings        := sys.env.get("GITHUB_ACTIONS").contains("true"),
    tlCiHeaderCheck        := true,
    tlCiScalafmtCheck      := true,
    tlCiScalafixCheck      := true,
    tlJdkRelease           := Some(8),
    organizationName       := "SiriusXM",
    startYear              := Some(2024),
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
  )
)

lazy val root = (project in file("."))
  .aggregate(allModules: _*)
  .enablePlugins(NoPublishPlugin)

lazy val allModules: Seq[ProjectReference] = Seq(
  Seq[ProjectReference](docs, plugin),
  hashing.projectRefs,
  core.projectRefs,
  weaver.projectRefs,
  munit.projectRefs,
  scalatest.projectRefs
).flatten

lazy val pluginSettings = Seq(
  scalaVersion                     := sbtPluginScalaVersion,
  sbtPluginPublishLegacyMavenStyle := false,
  scriptedLaunchOpts               := {
    scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
  },
  scriptedBufferLog := false
)

lazy val hashing = (projectMatrix in file("modules/hashing"))
  .settings(
    name := "snapshot4s-hashing",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "weaver-cats"       % Versions.weaver % Test,
      "org.typelevel" %%% "weaver-scalacheck" % Versions.weaver % Test
    ),
    mimaPreviousArtifacts := Set.empty
  )
  .jvmPlatform(scalaVersions = scalaVersions)
  .jsPlatform(scalaVersions = scalaVersions)

lazy val core = (projectMatrix in file("modules/core"))
  .settings(
    name := "snapshot4s-core",
    libraryDependencies ++= Seq(
      "com.lihaoyi"   %%% "pprint"                   % Versions.pprint,
      "org.typelevel" %%% "weaver-cats"              % Versions.weaver % Test,
      "org.scalameta" %%% "munit"                    % Versions.munit  % Test,
      "org.typelevel"  %% "scalac-compat-annotation" % Versions.scalacCompatAnnotation
    ) ++ scalaReflect(scalaVersion.value),
    mimaPreviousArtifacts := Set.empty
  )
  .dependsOn(hashing)
  .jvmPlatform(
    scalaVersions = scalaVersions,
    libraryDependencies += "com.lihaoyi" %% "os-lib" % Versions.oslib
  )
  .jsPlatform(
    scalaVersions = scalaVersions,
    // module support is required to run tests
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )

lazy val munit = (projectMatrix in file("modules/munit"))
  .settings(
    name := "snapshot4s-munit",
    libraryDependencies ++= Seq(
      "org.scalameta" %%% "munit"       % Versions.munit,
      "org.typelevel" %%% "weaver-cats" % Versions.weaver % Test
    ),
    mimaPreviousArtifacts := Set.empty
  )
  .dependsOn(core)
  .jvmPlatform(scalaVersions = scalaVersions)
  .jsPlatform(scalaVersions = scalaVersions)

lazy val weaver = (projectMatrix in file("modules/weaver"))
  .settings(
    name := "snapshot4s-weaver",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "weaver-cats" % Versions.weaver,
      "org.scalameta" %%% "munit"       % Versions.munit
    ),
    mimaPreviousArtifacts := Set.empty
  )
  .dependsOn(core)
  .jvmPlatform(scalaVersions = scalaVersions)
  .jsPlatform(scalaVersions = scalaVersions)

lazy val scalatest = (projectMatrix in file("modules/scalatest"))
  .settings(
    name := "snapshot4s-scalatest",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest-core" % Versions.scalatest,
      "org.typelevel" %%% "weaver-cats"    % Versions.weaver % Test
    ),
    mimaPreviousArtifacts := Set.empty
  )
  .dependsOn(core)
  .jvmPlatform(scalaVersions = scalaVersions)
  .jsPlatform(scalaVersions = scalaVersions)

// This filter finds the libraries required by the scripted tests.
lazy val scriptedScopeFilter = ScopeFilter(
  inProjects(
    Seq(hashing, core, weaver, munit, scalatest).flatMap(_.projectRefs): _*
  )
)

lazy val plugin = project
  .in(file("modules/plugin"))
  .enablePlugins(SbtPlugin, BuildInfoPlugin)
  .dependsOn(hashing.jvm(sbtPluginScalaVersion))
  .settings(
    name := "sbt-snapshot4s",
    buildInfoSettings,
    pluginSettings,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    mimaPreviousArtifacts := Set.empty,
    scriptedDependencies  := {
      scriptedDependencies.value
      publishLocal.all(scriptedScopeFilter).value
    }
  )

def latestStableVersion = {
  import scala.sys.process._
  "git -c versionsort.suffix=- tag --list --sort=-version:refname".!!.split("\n").toList
    .map(_.trim)
    .find(_.startsWith("v"))
    .map(_.replace("v", ""))
}

lazy val docs = project
  .in(file("docs"))
  .settings(
    tlMimaPreviousVersions := Set.empty,
    mdocIn                 := new File("docs/markdown/"),
    mdocOut                := new File("website/docs"),
    mdocVariables          := Map(
      "LATEST_STABLE_VERSION" -> latestStableVersion.getOrElse(version.value)
    ),
    fork := false, // Without this set to false mdoc would mess up it's paths and stop working
    libraryDependencies += "org.scalatest" %%% "scalatest" % Versions.scalatest
  )
  .dependsOn(
    core.jvm(scala3Version),
    weaver.jvm(scala3Version),
    munit.jvm(scala3Version),
    scalatest.jvm(scala3Version)
  )
  .enablePlugins(MdocPlugin, NoPublishPlugin)

addCommandAlias(
  "formatAll",
  List(
    "scalafmtAll",
    "scalafmtSbt",
    "scalafixAll"
  ).mkString(" ; ")
)

addCommandAlias(
  "validateFormatting",
  List(
    "scalafmtCheckAll",
    "scalafmtSbtCheck",
    "scalafixAll --check"
  ).mkString(" ; ")
)

addCommandAlias(
  "validate",
  List(
    "clean",
    "headerCheck",
    "Test/compile",
    "docs/mdoc --check",
    "hashing/test",
    "core/test",
    "munit/test",
    "weaver/test",
    "plugin/test",
    "docs/test",
    "scripted",
    "mimaReportBinaryIssues"
  ).mkString(" ; ")
)

addCommandAlias(
  "release",
  List(
    "package",
    "publish"
  ).mkString(" ; ")
)
