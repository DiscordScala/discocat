val catsCoreVersion = "2.0.0-M1"
val catsEffectVersion = "2.0.0-M1"
val fs2CoreVersion = "1.0.4"
val fs2HttpVersion = "0.4.1"
val circeVersion = "0.12.0-M1"
val circeFs2Version = "0.11.0"
val spireVersion = "0.16.1"

lazy val discocat = (project in file(".")).settings (
  organization := "org.discordscala",
  name := "discocat",
  version := "0.1.0",
  scalaVersion := "2.12.8",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % catsCoreVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "co.fs2" %% "fs2-core" % fs2CoreVersion,
    "com.spinoco" %% "fs2-http" % fs2HttpVersion,
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-generic-extras" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "io.circe" %% "circe-fs2" % circeFs2Version,
    "org.typelevel" %% "spire" % spireVersion,
  ),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Ypartial-unification",
    "-language:higherKinds",
  ),
  addCompilerPlugin(
    "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
  ),
)
