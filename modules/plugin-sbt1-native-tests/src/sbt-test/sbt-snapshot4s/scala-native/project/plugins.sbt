addSbtPlugin("org.scala-native" % "sbt-scala-native"          % "0.5.10")

sys.props.get("plugin.version") match {
  case Some(x) =>
    addSbtPlugin("com.siriusxm" % "sbt-snapshot4s" % x)
  case _ =>
    sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
