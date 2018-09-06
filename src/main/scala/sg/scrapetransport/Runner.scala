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
case class LTABusStopsResponse(`odata.metadata`: String, value: List[LTABusStops])

case class LTABusServies(ServiceNo: Int, Operator: String)
case class LTABusServicesResponse(`odata.metadata`: String, BusStopCode: String, Services: List[LTABusServies])

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
    val db = db_connect(conf)
    db.createSession()
    val busStops = TableQuery[BusStops]

    var startId = 0
    var stops : List[LTABusStops] = fetchBusstops(conf, startId)
    while (stops.length > 0) {
      val insertActions = DBIO.seq(
        busStops ++= stops
      )
      val resp = Try({Await.result(db.run(insertActions), 5 seconds)}) match {
        case Success(s) => s
        case Failure(ex) => {
          println("INSERT Failure ", ex)
          None
        }
      }
      startId += 500
      stops = fetchBusstops(conf, startId)
    }
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

  def fetchBusstops(conf: Config, startId: Int) : List[LTABusStops] = {
    val api_key = conf.getString("api_keys.lta_token")
    val r = requests.get(
      "http://datamall2.mytransport.sg/ltaodataservice/BusStops?$skip=" + startId,
      headers = Map("AccountKey" -> api_key)
    )
    decode[LTABusStopsResponse](r.text).right.get.value
  }
}
