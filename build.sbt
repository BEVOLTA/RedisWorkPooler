name := """Redis-scala"""

version := "0.1"

scalaVersion := "2.11.8"



resolvers ++= Seq(
   "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka"   %% "akka-stream"      % "2.4.7",
  "com.etaty.rediscala" %% "rediscala"        % "1.5.0",
  "ch.qos.logback"    %  "logback-classic"  % "1.1.3"
)

//fork in run := true
parallelExecution in Test := true