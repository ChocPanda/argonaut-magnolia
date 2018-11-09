// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `argonaut-magnolia` =
  project
    .in(file("."))
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.argonaut,
        library.magnolia,
        library.scalactic
      ),
      libraryDependencies ++= Seq(
        library.scalaCheck % Test,
        library.scalatest  % Test,
        library.utest      % Test
      )
    )
  .enablePlugins(AutomateHeaderPlugin)

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val scalaCheck = "1.14.0"
      val scalatest  = "3.0.5"
      val magnolia   = "0.10.0"
      val argonaut   = "6.2.2"
      val utest      = "0.6.5"
    }

    val magnolia   = "com.propensive" %% "magnolia"   % Version.magnolia
    val argonaut   = "io.argonaut"    %% "argonaut"   % Version.argonaut
    val scalactic  = "org.scalactic"  %% "scalactic"  % Version.scalatest
    val scalatest  = "org.scalatest"  %% "scalatest"  % Version.scalatest
    val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
    val utest      = "com.lihaoyi"    %% "utest"      % Version.utest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
commonSettings ++
fmtSettings ++
fixSettings ++
styleSettings

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.7",
    organization := "io.panda",
    organizationName := "Matt Searle",
    name := "Argonaut Magnolia",
    startYear := Some(2018),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding",
      "UTF-8",
      "-Ypartial-unification",
      "-Ywarn-unused-import"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    Compile / compile / wartremoverWarnings ++= Warts.unsafe
  )

lazy val fmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val fixSettings =
  Seq(
    libraryDependencies += compilerPlugin(scalafixSemanticdb),
    scalacOptions ++= Seq(
      "-Yrangepos",
      "-Ywarn-unused-import"
    )
  )

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
lazy val styleSettings = {
  Seq(
    scalastyleFailOnError := true,
    scalastyleFailOnWarning := true
  )
}

// *****************************************************************************
// Commands
// *****************************************************************************

addCommandAlias("fix", "; compile:scalafix; test:scalafix")
addCommandAlias("fixCheck", "; compile:scalafix --check; test:scalafix --check")
addCommandAlias("fmt", "; compile:scalafmt; test:scalafmt; scalafmtSbt")
addCommandAlias("fmtCheck", "; compile:scalafmtCheck; test:scalafmtCheck; scalafmtSbtCheck")
addCommandAlias("styleCheck", "; compile:scalastyle; test:scalastyle")
