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
        library.magnolia,
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
      val scalaCheck         = "1.14.0"
      val magnolia           = "0.10.0"
      val argonaut           = "6.2.3"
      val utest              = "0.6.7"
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

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.7",
    organization := "com.github.chocpanda",
    homepage := Option(url("https://github.com/ChocPanda/argonaut-magnolia")),
    name := "Argonaut Magnolia",
    startYear := Some(2018),
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
    scapegoatVersion in ThisBuild := "1.3.8",
    updateOptions := updateOptions.value.withGigahorse(false),
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
    addCompilerPlugin(scalafixSemanticdb),
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
addCommandAlias("fixcheck", "; compile:scalafix --check; test:scalafix --check")
addCommandAlias("fmt", "; compile:scalafmt; test:scalafmt; scalafmtSbt")
addCommandAlias("fmtcheck", "; compile:scalafmtCheck; test:scalafmtCheck; scalafmtSbtCheck")
addCommandAlias("stylecheck", "; compile:scalastyle; test:scalastyle")
