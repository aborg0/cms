package com.github.aborg0.cms

import com.github.aborg0.cms.params.Database
import zhttp.http.*
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio.Console.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigSource
import zio.{Console, ExitCode, System, URIO, ZEnv, ZIO, ZIOAppDefault, config}

import java.io.{File, IOException}

//import io.getquill.context.ZioJdbc.DataSourceLayer
//import zio.config.magnolia.descriptor
object HelloWorld extends ZIOAppDefault {

  override def run: URIO[ZEnv, ExitCode] =
    (for {
      _     <- application
      fi    <- myAppLogic.fork
      fiber <- (Server.app(httpApp.silent) ++ Server.port(8765)).make.useForever
                 .provideCustomLayer(ServerChannelFactory.auto ++ EventLoopGroup.auto())
                 .fork
      _     <- fi.join
      _     <- fiber.join
    } yield ()).exitCode

  val getDesc: ZIO[System, ReadError[String], ConfigDescriptor[Database]] =
    for {
      hoconFile <-
        ZIO
          .from(TypesafeConfigSource.fromHoconFile(new File("./cms/shared/src/main/resources/application.conf")))
          .mapError(t => ReadError.SourceError(t.getMessage))
//      constant  <- ZIO.fromEither(TypesafeConfigSource.fromHoconString(s""))
      env       <- ZIO.from(ConfigSource.fromSystemEnv()).mapError(t => ReadError.SourceError(t.getMessage))
      sysProp   <- ZIO.from(ConfigSource.fromSystemProps()).mapError(t => ReadError.SourceError(t.getMessage))
      source     = hoconFile <> /*constant <>*/ env <> sysProp
    } yield (Database.config from source)

  val application: ZIO[Console & System, String, /*ZLayer[Any, Nothing, Has[Datasource]]*/ Unit] =
    for {
      desc        <- getDesc.mapError(_.prettyPrint())
      docs         = generateDocs(desc)
      _           <- printLine(docs).orDie
      configValue <- config.read(desc).mapError(_.prettyPrint()).orDieWith { s =>
                       println(s)
                       new RuntimeException(s)
                     }
//      conn        <- DataSourceLayer.fromJdbcConfig(configValue.toQuillJdbcConfig)
      _            = println(configValue)
      _            = println(docs.toTable.toGithubFlavouredMarkdown)
      _            = println(generateReport(desc, configValue).map(docs => docs.toTable.toGithubFlavouredMarkdown))
//      _           = println(generateReport(desc, configValue).map(docs => docs.toTable/*.toGithubFlavouredMarkdown*/)/*.toHocon(descriptor[ConfigDocs].orElseEither(descriptor[String]))*/)
//      _ <- printLine(docs.toTable.toMarkdown((heading, n, either) => ???)).mapError(_.getMessage)
//      string      <- ZIO.from(configValue.toJson(desc))
//      _ <- printLine(string).mapError(_.getMessage)
    } yield ()

  val httpApp: HttpApp[Any, Nothing] = Http.collect[Request] { case Method.GET -> !! / "text" =>
    Response.text("Hello world")
  }

  val myAppLogic: ZIO[Console, IOException, Unit] =
    for {
      _    <- printLine("Hello! What is your name?")
      name <- readLine
      _    <- printLine(s"Hello, $name, welcome to ZIO!")
    } yield ()
}
