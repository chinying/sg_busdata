package sg.scrapetransport

import com.typesafe.config._

import scala.util.{Try, Success, Failure}

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import requests._
import scala.concurrent.{Await, Future}

import slick.jdbc.JdbcBackend.Database
import slick.driver.PostgresDriver.api._
import scala.concurrent.duration._
import slick.dbio.DBIO

case class LTABusStops(BusStopCode: String, RoadName: String, Description: String, Latitude: Double, Longitude: Double)
case class LTABusStopsResponse(`odata.metadata`: String, value: Array[LTABusStops])

case class LTABusServies(ServiceNo: Int, Operator: String)
case class LTABusServicesResponse(`odata.metadata`: String, BusStopCode: String, Services: Array[LTABusServies])

class BusStops (tag: Tag)
  extends Table[LTABusStops](tag, "busstops") {
  def busStopCode = column[String]("busstopcode")
  def roadName = column[String]("roadname")
  def description = column[String]("description")
  def latitude = column[Double]("latitude")
  def longitude = column[Double]("longitude")
  def * = (busStopCode, roadName, description, latitude, longitude) <> (LTABusStops.tupled, LTABusStops.unapply)
}


object Runner extends App {
  override def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load("config.conf")
    // val db = db_connect(conf)
    // db.createSession()
    // val busStops = TableQuery[BusStops]
    // val insertActions = DBIO.seq(
    //   // some random stop
    //   busStops += (new LTABusStops("08121", "Somerset Rd", "Somerset Stn", 1.30027569326585990, 103.83877618459662528))
    // )
    // val resp = Try({Await.result(db.run(insertActions), 5 seconds)}) match {
    //   case Success(s) => s
    //   case Failure(ex) => {
    //     println("INSERT Failure ", ex)
    //     None
    //   }
    // }
    // println(resp)

    fetchBusstops(conf).map(_.BusStopCode).foreach(println)
  }

  def db_connect(conf: Config) : Database = {
    val host = conf.getString("database.host")
    val username = conf.getString("database.username")
    val password = conf.getString("database.password")
    val database = conf.getString("database.dbname")

    Database.forURL(
      s"jdbc:postgresql://${host}/${database}?user=${username}&password=${password}",
      driver="org.postgresql.Driver")
  }

  def fetchBusstops(conf: Config) : Array[LTABusStops] = {
    val api_key = conf.getString("api_keys.lta_token")
    val r = requests.get(
      "http://datamall2.mytransport.sg/ltaodataservice/BusStops",
      // "http://datamall2.mytransport.sg/ltaodataservice/BusArrivalv2?BusStopCode=08121",
      headers = Map("AccountKey" -> api_key)
    )
    val blankArray = Array[Int]()
    val resp = decode[LTABusStopsResponse](r.text).right.get.value
    resp
  }
}
