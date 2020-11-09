organization in ThisBuild := "io.circe"

val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import",
  "-Xfuture"
)

val circeVersion = "0.11.2"
val previousCirceSprayVersion = "0.10.0"

val baseSettings = Seq(
  scalacOptions ++= compilerOptions,
  scalacOptions in (Compile, console) ~= {
    _.filterNot(Set("-Ywarn-unused-import"))
  },
  scalacOptions in (Test, console) ~= {
    _.filterNot(Set("-Ywarn-unused-import"))
  },
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  coverageHighlighting := true,
  (scalastyleSources in Compile) ++= (unmanagedSourceDirectories in Compile).value,
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-jawn" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion % Test,
    "io.circe" %% "circe-testing" % circeVersion % Test
  ),
  classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.AllLibraryJars
)

val docMappingsApiDir = settingKey[String]("Subdirectory in site target directory for API docs")

val allSettings = baseSettings ++ publishSettings

val root = project.in(file("."))
  .settings(allSettings ++ noPublishSettings)
  .aggregate(core)
  .dependsOn(core)

lazy val core = project.in(file("core"))
  .enablePlugins(GhpagesPlugin)
  .settings(allSettings)
  .settings(
    moduleName := "circe-spray",
    docMappingsApiDir := "api",
    addMappingsToSiteDir(mappings in (Compile, packageDoc), docMappingsApiDir),
    ghpagesNoJekyll := true,
    scalacOptions in (Compile, doc) ++= Seq(
      "-groups",
      "-implicits",
      "-doc-source-url", scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
      "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath
    ),
    git.remoteRepo := "git@github.com:circe/circe-spray.git",
    autoAPIMappings := true,
    apiURL := Some(url("https://circe.github.io/circe-spray/api/")),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.3.16",
      "io.spray" %% "spray-httpx" % "1.3.4",
      "io.spray" %% "spray-routing-shapeless23" % "1.3.4" % Test,
      "io.spray" %% "spray-testkit" % "1.3.4" % Test,
      "org.scalacheck" %% "scalacheck" % "1.15.0" % Test,
      "org.scalatest" %% "scalatest" % "3.2.3" % Test,
      "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % Test,
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" % Test cross CrossVersion.full)
    ),
    mimaPreviousArtifacts := Set("io.circe" %% "circe-spray" % previousCirceSprayVersion)
  )

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseVcsSign := true,
  homepage := Some(url("https://github.com/circe/circe-spray")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/circe/circe-spray"),
      "scm:git:git@github.com:circe/circe-spray.git"
    )
  ),
  developers := List(
    Developer(
      "travisbrown",
      "Travis Brown",
      "travisrobertbrown@gmail.com",
      url("https://twitter.com/travisbrown")
    )
  )
)

credentials ++= (
  for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    username,
    password
  )
).toSeq
