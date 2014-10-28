import sbt._
import sbt.Keys._

object SparkleanBuild extends Build {

  lazy val sparklean = Project(
    id = "sparklean",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "sparkLean",
      organization := "es.care.sf",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.2"
      // add other settings here
    )
  )
}
