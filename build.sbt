import sbtsonar.SonarPlugin.autoImport.sonarUseExternalConfig

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
          library.magnolia
        ),
      libraryDependencies ++= Seq(
          library.scalaCheck         % Test,
          library.scalaCheckMagnolia % Test,
          library.utest              % Test
        )
    )
    .enablePlugins(AutomateHeaderPlugin)
    .enablePlugins(GitBranchPrompt)

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val scalaCheck         = "1.14.2"
      val magnolia           = "0.12.0"
      val argonaut           = "6.2.3"
      val utest              = "0.7.1"
      val scalacheckMagnolia = "0.3.0"
    }

    val magnolia           = "com.propensive"       %% "magnolia"            % Version.magnolia
    val argonaut           = "io.argonaut"          %% "argonaut"            % Version.argonaut
    val scalaCheck         = "org.scalacheck"       %% "scalacheck"          % Version.scalaCheck
    val scalaCheckMagnolia = "com.github.chocpanda" %% "scalacheck-magnolia" % Version.scalacheckMagnolia
    val utest              = "com.lihaoyi"          %% "utest"               % Version.utest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  fmtSettings ++
  fixSettings ++
  styleSettings

def versionedSettings(scalaVersion: String) = CrossVersion.partialVersion(scalaVersion) match {
  case Some((2, n)) if n <= 12 => Seq("-Ypartial-unification", "-Ywarn-unused-import", "-Yrangepos")
  case _                       => Seq()
}

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.10", "2.13.0"
    name := "Argonaut Magnolia",
    organization := "com.github.chocpanda",
    turbo := true,
    sonarUseExternalConfig := true,
    homepage := Option(url("https://github.com/ChocPanda/argonaut-magnolia")),
    startYear := Option(2018),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
        Developer(
          "ChocPanda",
          "Matt Searle",
          "mattsearle@ymail.com",
          url("https://github.com/ChocPanda/")
        )
      ),
    sonarUseExternalConfig := true,
    scapegoatVersion in ThisBuild := "1.3.10",
    updateOptions := updateOptions.value.withGigahorse(false),
    scalacOptions ++= Seq(
        "-unchecked",
        "-deprecation",
        "-language:_",
        "-encoding",
        "UTF-8"
      ) ++ versionedSettings(scalaVersion.value),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    Compile / unmanagedSourceDirectories := Seq(
        (Compile / scalaSource).value,
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, n)) if n >= 13 => baseDirectory.value / "src" / "main" / "scala-2.13+"
          case _                       => baseDirectory.value / "src" / "main" / "scala-2.13-"
        }
      ),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    Compile / compile / wartremoverWarnings ++= Warts.unsafe
  )

lazy val fmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val fixSettings =
  Seq(
    addCompilerPlugin(scalafixSemanticdb)
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
addCommandAlias("fixcheck", "; compile:scalafix --check; test:scalafix --check")
addCommandAlias("fmt", "; compile:scalafmt; test:scalafmt; scalafmtSbt")
addCommandAlias("fmtcheck", "; compile:scalafmtCheck; test:scalafmtCheck; scalafmtSbtCheck")
addCommandAlias("stylecheck", "; compile:scalastyle; test:scalastyle")
