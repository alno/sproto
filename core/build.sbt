
organization := "org.sproto"

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.8.2" % "test",
  "org.scalatest" %% "scalatest" % "1.6.1" % "test"
)

sourceGenerators in Compile <+= (fmpp).task

