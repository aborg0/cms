package com.github.aborg0.cms

import zio.*
import zio.Console
import zio.test.Assertion.*
import zio.test.*

object HelloWorld {
  def sayHello: ZIO[Console, Nothing, Unit] =
    Console.printLine("Hello, World!").orDie
}

object HelloWorldSpec extends DefaultRunnableSpec {

  import HelloWorld.*

  def spec: ZSpec[Environment, Failure] = suite("HelloWorldSpec")(
    test("sayHello correctly displays output") {
      for {
        _      <- sayHello
        output <- TestConsole.output
      } yield assert(output)(equalTo(Vector("Hello, World!\n")))
    }
  )
}
