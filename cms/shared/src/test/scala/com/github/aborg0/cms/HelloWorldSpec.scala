package com.github.aborg0.cms

import zio.Console.ConsoleLive
import zio.test.Assertion.*
import zio.test.*
import zio.test.Spec.empty.ZSpec
import zio.{Console, *}

object HelloWorld {
  def sayHello: ZIO[Console, Nothing, Unit] =
    Console.printLine("Hello, World!").orDie
}

object HelloWorldSpec extends ZIOSpecDefault {

  import HelloWorld.*

  def spec: Spec[TestEnvironment with Scope, Nothing] = suite("HelloWorldSpec")(
    test("sayHello correctly displays output") {
      for {
        _      <- sayHello
        output <- TestConsole.output
      } yield assertTrue(output == Vector("Hello, World!\n"))
    }
  ).provideEnvironment(DefaultServices.live)
}
