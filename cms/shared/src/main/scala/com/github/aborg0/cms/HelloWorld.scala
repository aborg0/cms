package com.github.aborg0.cms

import zio.Console
import zio.Console.*
import zio.{ZIOAppDefault, ExitCode, URIO, ZIO}

import java.io.IOException

object HelloWorld extends ZIOAppDefault {

  override def run/*(args: List[String])*/: URIO[Console, ExitCode] =
    myAppLogic.exitCode

  val myAppLogic: ZIO[Console, IOException, Unit] =
    for {
      _    <- printLine("Hello! What is your name?")
      name <- readLine
      _    <- printLine(s"Hello, $name, welcome to ZIO!")
    } yield ()
}
