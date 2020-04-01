val libs = org.typelevel.libraries
  .add(name = "cats", version = "2.1.1")
  .add(name = "cats-effect", version = "2.1.2")
  .add(name = "fs2", version = "2.3.0")
  .add(name = "http4s", version = "0.21.2")
  .add(name = "circe", version = "0.13.0", org = "io.circe", "circe-core", "circe-generic", "circe-generic-extras", "circe-parser", "circe-fs2")
  .add(name = "spire", version = "0.17.0-M1", org = "org.typelevel", "spire")
  .add(name = "http4s-jdk-http-client", version = "0.2.0", org = "org.http4s", "http4s-jdk-http-client")

lazy val discocat = (project in file("discocat")).settings(
  organization := "org.discordscala",
  name := "discocat",
  version := "0.1.0",
  scalaVersion := "2.13.1",
  libs.dependencies(
    "cats-core",
    "cats-effect",
    "fs2-core",
    "circe-core",
    "circe-generic",
    "circe-generic-extras",
    "circe-parser",
    "circe-fs2",
    "http4s-jdk-http-client",
    "spire",
  ),
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
)
