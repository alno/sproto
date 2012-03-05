import sbt._
import Keys._
import fmpp.FmppPlugin._

object SProtoBuild extends Build {

  lazy val root = Project(id = "sproto", base = file(".")) aggregate(core, mongo)

  lazy val core = Project(id = "sproto-core", base = file("core"), settings = Defaults.defaultSettings ++ fmppSettings) configs(Fmpp)

  lazy val mongo = Project(id = "sproto-mongo", base = file("mongo")) dependsOn(core)

}
