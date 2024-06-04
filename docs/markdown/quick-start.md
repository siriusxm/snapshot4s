---
sidebar_position: 2
---

# Quick start

Add the SXM resolvers to `plugins.sbt`.

```scala
val artifactoryHost      = sys.env.getOrElse("ARTIFACTORY_HOST", "cpartifactory.corp.siriusxm.com")
val artifactoryRepoHost  = s"https://$artifactoryHost/artifactory"
val artifactoryMavenRepo = s"$artifactoryRepoHost/maven"
val artifactoryMavenSnapshotRepo = s"$artifactoryRepoHost/maven-snapshots"

lazy val creds: Credentials =
  (sys.env.get("ARTIFACTORY_USER"), sys.env.get("ARTIFACTORY_TOKEN")) match {
    case (Some(user), Some(token)) =>
      Credentials("Artifactory Realm", artifactoryHost, user, token)
    case _ => Credentials(Path.userHome / ".sbt" / "credentials")
  }

credentials += creds

resolvers ++= Seq(
  "Maven artifactory" at artifactoryMavenRepo,
  "Maven snapshot artifactory" at artifactoryMavenSnapshotRepo
)
```

Add the plugin to `plugins.sbt`.

```scala
addSbtPlugin("com.siriusxm" % "sbt-snapshot4s" % "@LATEST_STABLE_VERSION@")
```

Enable it in `build.sbt`.

```scala
val root = (project in file(".")).enablePlugins(Snapshot4sPlugin)
```

Add the integration library for your test framework. See [supported frameworks](supported-frameworks.md) for more.

```scala
import snapshot4s.BuildInfo.snapshot4sVersion

libraryDependencies += "com.siriusxm" %% "snapshot4s-munit" % snapshot4sVersion % Test
```

Write a test.

```scala mdoc:invisible:reset
// The munit location macro can't calculate the source location of the md file.
// This implicit circumvents it.
import munit.Location
implicit val sourceloc: Location = Location.empty
```

```scala mdoc
import snapshot4s.munit.SnapshotAssertions
import snapshot4s.generated.snapshotConfig

object MySuite extends munit.FunSuite with SnapshotAssertions {
  test("snapshot4s can fill in the blanks") {
    val mySnapshotWorkflow = "snapshot4s"
    assertInlineSnapshot(mySnapshotWorkflow, ???)
  }
  test("snapshot4s can update") {
    val mySnapshotWorkflow = "snapshot4s"
    assertInlineSnapshot(mySnapshotWorkflow, "A ton of manual copy/pasting")
  }
  test("snapshot4s can work with files") {
    val mySnapshotWorkflow = "snapshot4s"
    assertFileSnapshot(mySnapshotWorkflow, "mySnapshotWorkflow")
  }
}
```

Run `test` and watch your test fail.
Run `snapshot4sPromote` and be bedazzled. You should now see:
```scala mdoc:invisible
import MySuite.*
```
```scala mdoc
test("should use snapshot4s") {
  val mySnapshotWorkflow = "snapshot4s"
  assertInlineSnapshot(mySnapshotWorkflow, "snapshot4s") // <- spot the difference
}
```

as well as a new `mySnapshotWorkflow` file.

Run `test` again and watch them pass.
