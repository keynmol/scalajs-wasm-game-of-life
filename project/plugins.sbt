addSbtPlugin("com.indoorvivants" % "sbt-commandmatrix" % "0.0.5")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % sys.env.getOrElse("SCALAJS_VERSION", "1.19.0"))
