package com.github.aborg0.cms.params

import com.typesafe.config.ConfigFactory
import io.getquill.JdbcContextConfig
import zio.config.ConfigDescriptor.*
import zio.config.*

import java.util.Properties
import scala.concurrent.duration.Duration

final class Database(
  val datasourceClassName: String,
  val user: String,
  val password: String,
  val databaseName: String,
  val portNumber: Int,
  val serverName: String,
  val connectionTimeout: Option[Duration],
  val minimumPoolSize: Option[Int],
  val leakDetectionThreshold: Option[Int]
) {
  def toQuillJdbcConfigProperties: Properties = {
    val res = new Properties()
    res.put("datasourceClassName", datasourceClassName)
    res.put("user", user)
    res.put("password", password)
    res.put("databaseName", databaseName)
    res.put("portNumber", portNumber)
    res.put("serverName", serverName)
    connectionTimeout.foreach(d => res.put("connectionTimeout", d.toMillis))
    minimumPoolSize.foreach(n => res.put("minimumPoolSize", n))
    leakDetectionThreshold.foreach(n => res.put("leakDetectionThreshold", n))
    res
  }

  def toQuillJdbcConfig: JdbcContextConfig = JdbcContextConfig(
    ConfigFactory.parseProperties(toQuillJdbcConfigProperties)
  )

  override def toString: String =
    s"""
       |datasourceClassName: $datasourceClassName
       |user: $user
       |password: ******
       |databaseName: $databaseName
       |portNumber: $portNumber
       |serverName: $serverName
       |connectionTimeout: $connectionTimeout
       |minimumPoolSize: $minimumPoolSize
       |leakDetectionThreshold: $leakDetectionThreshold
       |""".stripMargin
}

object Database {
  def fromTuple(
    t: (String, String, String, String, Int, String, Option[Duration], Option[Int], Option[Int])
  ): Database                                                                                                      =
    new Database(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9)
  def apply(
    datasourceClassName: String,
    user: String,
    password: String,
    databaseName: String,
    portNumber: Int,
    serverName: String,
    connectionTimeout: Option[Duration],
    minimumPoolSize: Option[Int],
    leakDetectionThreshold: Option[Int]
  ): Database                                                                                                      =
    new Database(
      datasourceClassName,
      user,
      password,
      databaseName,
      portNumber,
      serverName,
      connectionTimeout,
      minimumPoolSize,
      leakDetectionThreshold
    )
  def applyTupled(
    t: ((((((((String, String), String), String), Int), String), Option[Duration]), Option[Int]), Option[Int])
  ): Database                                                                                                      =
    new Database(
      t._1._1._1._1._1._1._1._1,
      t._1._1._1._1._1._1._1._2,
      t._1._1._1._1._1._1._2,
      t._1._1._1._1._1._2,
      t._1._1._1._1._2,
      t._1._1._1._2,
      t._1._1._2,
      t._1._2,
      t._2
    )
  def applyTupledEither(
    t: ((((((((String, String), String), String), Int), String), Option[Duration]), Option[Int]), Option[Int])
  ): Either[String, Database]                                                                                      =
    Right(
      new Database(
        t._1._1._1._1._1._1._1._1,
        t._1._1._1._1._1._1._1._2,
        t._1._1._1._1._1._1._2,
        t._1._1._1._1._1._2,
        t._1._1._1._1._2,
        t._1._1._1._2,
        t._1._1._2,
        t._1._2,
        t._2
      )
    )
  def unapply(
    db: Database
  ): Some[((((((((String, String), String), String), Int), String), Option[Duration]), Option[Int]), Option[Int])] =
    Some(
      (
        (
          (
            (((((db.datasourceClassName, db.user), db.password), db.databaseName), db.portNumber), db.serverName),
            db.connectionTimeout
          ),
          db.minimumPoolSize
        ),
        db.leakDetectionThreshold
      )
    )
  def toTuple(
    db: Database
  ): ((((((((String, String), String), String), Int), String), Option[Duration]), Option[Int]), Option[Int])       = unapply(
    db
  ).get
  val config: ConfigDescriptor[Database]                                                                           = (string("datasourceClassName") <*> string("user") <*>
    string("password") <*> string("databaseName") <*>
    int("portNumber") <*> string("serverName") <*>
    duration("connectionTimeout").optional <*> int("minimumPoolSize").optional <*>
    int("leakDetectionThreshold").optional).transform(Database.applyTupled, Database.toTuple)
}
