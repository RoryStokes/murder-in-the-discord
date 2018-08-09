val javaDeps = Seq(
  "net.dv8tion" % "JDA" % "3.7.1_386"
)

val scalaDeps = Seq(
  "io.suzaku" %% "diode" % "1.1.3"
)

resolvers += Resolver.jcenterRepo

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "es.rorystok",
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    )),
    scalacOptions ++= Seq("-language:implicitConversions"),
    name := "murder-in-the-discord",
    libraryDependencies ++= javaDeps ++ scalaDeps
  )
