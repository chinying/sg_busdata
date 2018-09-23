package sg.scrapetransport

import slick.jdbc.JdbcBackend.Database
import slick.driver.PostgresDriver.api._
import slick.dbio.DBIO

import scala.util.{Try, Success, Failure}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


object DatabaseUtils {
  val conf = AppConfig.conf

  def db_connect() : Database = {
    val host = conf.getString("database.host")
    val username = conf.getString("database.username")
    val password = conf.getString("database.password")
    val database = conf.getString("database.dbname")

    Database.forURL(
      s"jdbc:postgresql://${host}/${database}?user=${username}&password=${password}",
      driver="org.postgresql.Driver")
  }

  // TODO: figure out how to pass List type
  def writeToDB[A <: slick.lifted.AbstractTable[_]]
    (db: Database, tableQuery: TableQuery[A], items: List[A#TableElementType]) {

  }
}
