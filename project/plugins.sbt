addDependencyTreePlugin

resolvers += "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots/"
addSbtPlugin("org.scalameta"    % "sbt-scalafmt"              % "2.5.6")
addSbtPlugin("org.scalameta"    % "sbt-mdoc"                  % "2.7.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"               % "0.6.4")
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.3.1")
addSbtPlugin("org.typelevel"    % "sbt-typelevel-scalafix"    % "0.8.0")
addSbtPlugin("org.typelevel"    % "sbt-typelevel-settings"    % "0.8.0")
addSbtPlugin("org.typelevel"    % "sbt-typelevel-ci-release"  % "0.8.0")
addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"             % "0.13.1")
addSbtPlugin("com.eed3si9n"     % "sbt-projectmatrix"         % "0.11.0")
addSbtPlugin("org.scala-js"     % "sbt-scalajs"               % "1.20.1")
addSbtPlugin("com.github.sbt"   % "sbt-header"                % "5.11.0")
