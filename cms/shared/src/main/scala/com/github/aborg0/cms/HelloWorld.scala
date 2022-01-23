package com.github.aborg0.cms

import com.github.aborg0.cms.params.Database
import zhttp.http.*
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio.console.Console.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigSource
import zio.console.{Console, getStrLn, putStrLn}
import zio.system.*
import zio.{ExitCode, URIO, ZEnv, ZIO, config}

import java.io.{File, IOException}
import scala.util.Try

//import io.getquill.context.ZioJdbc.DataSourceLayer
//import zio.config.magnolia.descriptor
object HelloWorld extends zio.App {

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
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
          .fromEither(Try(TypesafeConfigSource.fromHoconFile(new File(
            "./cms/shared/src/main/resources/application.conf"))).toEither).orDie
//      constant  <- ZIO.fromEither(TypesafeConfigSource.fromHoconString(s""))
      env       <- ZIO.fromEither(Try(ConfigSource.fromSystemEnv()).toEither).orDie
      sysProp   <- ZIO.fromEither(Try(ConfigSource.fromSystemProps()).toEither).orDie
      source     = hoconFile <> /*constant <>*/ env <> sysProp
    } yield (Database.config from source)

  val application: ZIO[Console & System, String, /*ZLayer[Any, Nothing, Has[Datasource]]*/ Unit] =
    for {
      desc        <- getDesc.mapError(_.prettyPrint())
      docs         = generateDocs(desc)
      _           <- putStrLn(docs.toString).orDie
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
      _    <- putStrLn("Hello! What is your name?")
      name <- getStrLn
      _    <- putStrLn(s"Hello, $name, welcome to ZIO!")
    } yield ()
}
