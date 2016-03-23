lazy val root = (project in file(".")).
  settings(
    name := "scala-solution",
    version := "1.1",
    scalaVersion := "2.11.7",
    libraryDependencies += "org.codecraftgame" %% "codecraft" % "0.3.0.0"
  )

