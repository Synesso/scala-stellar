ThisBuild / homepage := Some(url("https://github.com/synesso/scala-stellar"))
ThisBuild / scalaVersion := "2.13.6"

lazy val `scala-stellar` = project
  .in(file("."))
  .enablePlugins(ScalaUnidocPlugin)
  .aggregate(protocol, horizon)

lazy val protocol = project
  .in(file("protocol"))
  .settings(
    resolvers ++= List(
      Resolver.jcenterRepo,
      "jitpack" at "https://jitpack.io"
    ),
    libraryDependencies ++= List(
      "com.github.synesso" % "stellar-xdr-jre" % "17.0.0",
      "com.gu" %% "spy" % "0.1.1",
      "com.squareup.okio" % "okio" % "2.8.0",
      "commons-codec" % "commons-codec" % "1.15",
      "org.typelevel" %% "cats-core" % "2.6.1",
      "net.i2p.crypto" % "eddsa" % "0.3.0",
      "com.squareup.okio" % "okio" % "2.10.0",
    ) ::: logging ::: specs2,
    scalacOptions ++= List("-deprecation", "-feature"),
    coverage(95)
  )

lazy val horizon = project
  .in(file("horizon"))
  .dependsOn(protocol % "compile->compile;test->test")
  .enablePlugins(BuildInfoPlugin).settings(
    buildInfoPackage := "stellar"
  )
  .settings(
    libraryDependencies ++= List(
      "com.squareup.okhttp3" % "okhttp" % "4.9.1",
      "org.json4s" %% "json4s-native" % "4.0.1",
    ),
    scalacOptions ++= List("-deprecation", "-feature"),
    coverage(95),
    Test / parallelExecution := !scala.sys.env.get("PARALLEL_SPEC_EXECUTION").contains("false")
  )

val logging = List(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3",
  "org.slf4j" % "slf4j-simple" % "2.0.0-alpha1" % Test
)
val specs2 = List(
  "org.specs2" %% "specs2-core" % "4.12.0" % "test",
  "org.specs2" %% "specs2-scalacheck" % "4.12.0" % "test",
)

def coverage(min: Int) = List(
  coverageMinimumStmtTotal := min,
  coverageFailOnMinimum := true,
)
