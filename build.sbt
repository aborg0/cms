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
addCommandAlias("fix", "; all compile:scalafix test:scalafix; all scalafmtSbt scalafmtAll")
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

val zioVersion = "1.0.13"// "2.0.0-RC1"

lazy val root = project
  .in(file("."))
  .settings(
    publish / skip := true,
    unusedCompileDependenciesFilter -= moduleFilter("org.scala-js", "scalajs-library")
  )
  .aggregate(
    cmsJVM,
//    cmsJS,
//    cmsNative,
    docs
  )

lazy val cms = crossProject(/*JSPlatform,*/ JVMPlatform/*, NativePlatform*/)
  .in(file("cms"))
  .settings(stdSettings("cms"))
  .settings(crossProjectSettings)
  .settings(buildInfoSettings("cms"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio"          % zioVersion,
      "dev.zio" %%% "zio-logging"  % "0.5.14",
//      "dev.zio" %%% "zio-telemetry"% "0.9.0",
      "dev.zio" %%% "zio-opentelemetry" % "0.9.0",
      "dev.zio" %%% "zio-config-magnolia" % "1.0.10",
      "dev.zio" %%% "zio-test"     % zioVersion % Test,
      "dev.zio" %%% "zio-test-sbt" % zioVersion % Test
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
