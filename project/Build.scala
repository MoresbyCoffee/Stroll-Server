import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "MoresbyStroll"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      // Add your project dependencies here,
	  "org.reactivemongo" %% "reactivemongo" % "0.9",
      "mysql" % "mysql-connector-java" % "5.1.21",
      "org.scalatest" %% "scalatest" % "1.9.1" % "test",
      "com.typesafe.akka" %% "akka-testkit" % "2.1.0" % "test",
      "com.github.athieriot" % "specs2-embedmongo_2.9.1" % "0.5" % "test", //MIT
      "org.scalaj" %% "scalaj-http" % "0.3.7" //Apache 2
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
      testOptions in Test += Tests.Argument("junitxml", "console")
    )

}
