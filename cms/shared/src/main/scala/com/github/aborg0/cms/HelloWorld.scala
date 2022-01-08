package com.github.aborg0.cms

import zio.console._
import zio.{App, ExitCode, URIO, ZIO}

import java.io.IOException

object HelloWorld extends App {

  override def run(args: List[String]): URIO[Console, ExitCode] =
    myAppLogic.exitCode

  val myAppLogic: ZIO[Console, IOException, Unit] =
    for {
      _    <- putStrLn("Hello! What is your name?")
      name <- getStrLn
      _    <- putStrLn(s"Hello, $name, welcome to ZIO!")
    } yield ()
}
