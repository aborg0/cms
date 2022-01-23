import BuildHelper._

inThisBuild(
  List(
    organization := "com.github.aborg0",
    homepage := Some(url("https://github.com/aborg0/cms/")),
    licenses := List("AGPL-3" -> url("https://www.gnu.org/licenses/agpl-3.0.en.html")),
    developers := List(
      Developer(
        "aborg0",
        "Gabor Bakos",
        "bakos.gabor@mind-era.com",
        url("http://mind-era.com")
      )
    ),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc")
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
//addCommandAlias("fix", "; all compile:scalafix test:scalafix; all scalafmtSbt scalafmtAll")
addCommandAlias(
  "fix",
  "; cmsJVM / Compile / scalafix; docs / Compile / scalafix; Compile / scalafix; cmsJVM / Test / scalafix; docs / Test / scalafix; Test / scalafix"
)
addCommandAlias("check", "; scalafmtSbtCheck; scalafmtCheckAll; compile:scalafix --check; test:scalafix --check")

addCommandAlias(
  "testJVM",
  ";cmsJVM/test"
)
addCommandAlias(
  "testJS",
  ";cmsJS/test"
)
addCommandAlias(
  "testNative",
  ";cmsNative/test:compile"
)

val zioVersion = "2.0.0-RC1" // "1.0.13"

lazy val root = project
  .in(file("."))
  .settings(
    name := "cms",
    publish / skip := true,
    unusedCompileDependenciesFilter -= moduleFilter("org.scala-js", "scalajs-library")
  )
  .aggregate(
    cmsJVM,
//    cmsJS,
//    cmsNative,
    docs
  )

lazy val cms = crossProject( /*JSPlatform,*/ JVMPlatform /*, NativePlatform*/ )
  .in(file("cms"))
  .settings(stdSettings("cms"))
  .settings(crossProjectSettings)
  .settings(buildInfoSettings("cms"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio"      %%% "zio"                     % zioVersion,
      "dev.zio"      %%% "zio-cache"               % "0.2.0-RC1",
      "dev.zio"      %%% "zio-concurrent"          % zioVersion,
      "dev.zio"      %%% "zio-config-gen"          % "3.0.0-RC1",
//      "dev.zio" %%% "zio-logging"         % "0.5.14",
//      "dev.zio" %%% "zio-telemetry"% "0.9.0",
//      "dev.zio" %%% "zio-opentelemetry"   % "0.9.0",
      "dev.zio"      %%% "zio-config-magnolia"     % "3.0.0-RC1", // "1.0.10",
      "dev.zio"      %%% "zio-json-interop-http4s" % "0.3.0-RC1-1",
      "dev.zio"      %%% "zio-optics"              % "0.2.0-RC1",
      "dev.zio"      %%% "zio-process"             % "0.7.0-RC1",
      "dev.zio"      %%% "zio-query"               % "0.3.0-RC1",
      "dev.zio"      %%% "zio-schema-json"         % "0.2.0-RC1",
      "dev.zio"      %%% "zio-schema-derivation"   % "0.2.0-RC1",
      "dev.zio"      %%% "zio-schema-zio-test"     % "0.2.0-RC1-1" % Test,
      "dev.zio"      %%% "zio-streams"             % zioVersion,
      "dev.zio"      %%% "zio-test"                % zioVersion    % Test,
      "dev.zio"      %%% "zio-test-sbt"            % zioVersion    % Test,
      "io.d11"       %%% "zhttp"                   % zioVersion,
      "io.getquill"  %%% "quill-jdbc-zio"          % "3.12.0",
//      "io.github.kitlangton" %%% "zio-magic"      % "0.3.11",
      "org.postgresql" % "postgresql"              % "42.3.1"
    )
  )
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))
  .enablePlugins(BuildInfoPlugin)

//lazy val cmsJS = cms.js
//  .settings(jsSettings)
//  .settings(libraryDependencies += "dev.zio" %%% "zio-test-sbt" % zioVersion % Test)
//  .settings(scalaJSUseMainModuleInitializer := true)

lazy val cmsJVM = cms.jvm
  .settings(dottySettings)
  .settings(libraryDependencies += "dev.zio" %%% "zio-test-sbt" % zioVersion % Test)
  .settings(scalaReflectTestSettings)
  .enablePlugins(DockerPlugin)

//lazy val cmsNative = cms.native
//  .settings(nativeSettings)

lazy val docs = project
  .in(file("cms-docs"))
  .settings(stdSettings("cms"))
  .settings(
    publish / skip := true,
    moduleName := "cms-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(cmsJVM),
    ScalaUnidoc / unidoc / target := (LocalRootProject / baseDirectory).value / "website" / "static" / "api",
    cleanFiles += (ScalaUnidoc / unidoc / target).value,
    docusaurusCreateSite := docusaurusCreateSite.dependsOn(Compile / unidoc).value,
    docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(Compile / unidoc).value
  )
  .dependsOn(cmsJVM)
  .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)
