addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.10.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs"       % "1.17.0")

sys.props.get("plugin.version") match {
  case Some(x) =>
    addSbtPlugin("com.siriusxm" % "sbt-snapshot4s" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
