addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.18.2")

sys.props.get("plugin.version") match {
  case Some(x) =>
    addSbtPlugin("com.siriusxm" % "sbt-snapshot4s" % x)
  case _ =>
    sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
