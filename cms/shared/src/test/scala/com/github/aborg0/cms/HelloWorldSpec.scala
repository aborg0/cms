package com.github.aborg0.cms

import zio.*
import zio.console.{Console, putStrLn}
import zio.test.Assertion.*
import zio.test.*
import zio.test.environment.TestConsole

object HelloWorld {
  def sayHello: ZIO[Console, Nothing, Unit] =
    putStrLn("Hello, World!").orDie
}

object HelloWorldSpec extends DefaultRunnableSpec {

  import HelloWorld.*

  def spec: ZSpec[Environment, Failure] = suite("HelloWorldSpec")(
    testM("sayHello correctly displays output") {
      for {
        _      <- sayHello
        output <- TestConsole.output
      } yield assert(output)(equalTo(Vector("Hello, World!\n")))
    }
  )
}
