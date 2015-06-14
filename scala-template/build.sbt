lazy val root = (project in file(".")).
  settings(
    name := "codecraft-template",
    version := "1.0",
    scalaVersion := "2.11.4"
  )


libraryDependencies ++= Seq(
  "org.jogamp.gluegen" % "gluegen-rt" % "2.2.4"
)

