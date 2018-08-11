val commonSettings = Seq(
  resolvers += DefaultMavenRepository,
  scalaVersion := "2.12.6",
  version := "1.0"
)

val circeVersion = "0.9.3"

// circe json
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)


libraryDependencies ++= Seq(
  // env variables
  "com.typesafe" % "config" % "1.3.2",

  // database
  "org.postgresql" % "postgresql" % "42.2.4",
  "com.typesafe.slick" %% "slick" % "3.2.3",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",

  // utils
  "com.lihaoyi" %% "requests" % "0.1.2",
)
