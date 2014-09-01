import scalariform.formatter.preferences._

name := "safran-api"

organization := "net.fehmicansaglam"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.2"

scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-encoding", "utf8",
  "-feature",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-language:existentials")


resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/")

libraryDependencies ++= {
  val akkaV = "2.3.5"
  val sprayV = "1.3.1"
  Seq(
    "org.scala-lang.modules" %% "scala-xml"      % "1.0.2",
    "io.spray"               %%  "spray-can"     % sprayV,
    "io.spray"               %%  "spray-routing" % sprayV,
    "io.spray"               %%  "spray-json"    % "1.2.6",
    "com.typesafe.akka"      %%  "akka-actor"    % akkaV)
}

Revolver.settings

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignParameters, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
