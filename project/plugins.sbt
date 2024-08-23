addDependencyTreePlugin

resolvers += "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("org.scalameta"     % "sbt-scalafmt"              % "2.5.2")
addSbtPlugin("org.scalameta"     % "sbt-mdoc"                  % "2.5.4")
addSbtPlugin("com.timushev.sbt"  % "sbt-updates"               % "0.6.4")
addSbtPlugin("com.github.cb372"  % "sbt-explicit-dependencies" % "0.3.1")
addSbtPlugin("org.typelevel"     % "sbt-typelevel-scalafix"    % "0.7.2-12-17ac909-SNAPSHOT")
addSbtPlugin("org.typelevel"     % "sbt-typelevel-settings"    % "0.7.2-12-17ac909-SNAPSHOT")
addSbtPlugin("org.typelevel"     % "sbt-typelevel-ci-release"  % "0.7.2-12-17ac909-SNAPSHOT")
addSbtPlugin("com.eed3si9n"      % "sbt-buildinfo"             % "0.12.0")
addSbtPlugin("com.eed3si9n"      % "sbt-projectmatrix"         % "0.10.0")
addSbtPlugin("org.scala-js"      % "sbt-scalajs"               % "1.16.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header"                % "5.10.0")
