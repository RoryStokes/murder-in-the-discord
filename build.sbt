import Dependencies._

resolvers += Resolver.jcenterRepo
resolvers += "jitpack.io" at "https://jitpack.io"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "es.rorystok",
      scalaVersion := "0.9.0-RC1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "murder-in-the-discord",
    libraryDependencies ++= Seq(
      "net.dv8tion" % "JDA" % "3.7.1_386"
    )
  )