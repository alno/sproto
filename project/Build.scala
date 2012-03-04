import sbt._
import Keys._

object SProtoBuild extends Build {

  lazy val root = Project(id = "sproto", base = file(".")) aggregate(core, mongo)

  lazy val core = Project(id = "sproto-core", base = file("core"))

  lazy val mongo = Project(id = "sproto-mongo", base = file("mongo")) dependsOn(core)

}
