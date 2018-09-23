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

class BusStopsTable (tag: Tag)
  extends Table[LTABusStops](tag, "busstops") {
  def busStopCode = column[String]("busstopcode")
  def roadName = column[String]("roadname")
  def description = column[String]("description")
  def latitude = column[Double]("latitude")
  def longitude = column[Double]("longitude")
  def * = (busStopCode, roadName, description, latitude, longitude) <> (LTABusStops.tupled, LTABusStops.unapply)
}

class BusServiceStopTable (tag: Tag)
  extends Table[LTABusServiceStop] (tag, "busservicesstop") {
  def serviceNo = column[String]("serviceno")
  def operator = column [String]("operator")
  def direction = column[Int]("direction")
  def stopSequence = column[Int]("stopsequence")
  def busStopCode = column[String]("busstopcode")
  def busStop = foreignKey("busStopFK", busStopCode, TableQuery[BusStopsTable]) (_.busStopCode,
    onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def distance = column[Double]("distance")
  def weekdayFirstBus = column[String]("weekdayfirstbus")
  def weekdayLastBus = column[String]("weekdaylastbus")
  def satFirstBus = column[String]("satfirstbus")
  def satLastBus = column[String]("satlastbus")
  def sunFirstBus = column[String]("sunfirstbus")
  def sunLastBus = column[String]("sunlastbus")
  def * = (serviceNo, operator, direction, stopSequence, busStopCode, distance,
    weekdayFirstBus, weekdayLastBus, satFirstBus, satLastBus, sunFirstBus, sunLastBus) <> (LTABusServiceStop.tupled, LTABusServiceStop.unapply)
}

trait LTADatabaseSession {
  val conf = AppConfig.conf
  val api_key = conf.getString("api_keys.lta_token")
  val db = DatabaseUtils.db_connect()
  db.createSession()
}

object BusStopsBuilder extends LTADatabaseSession {

  val busStops = TableQuery[BusStopsTable]

  var startId = 0
  var stops : List[LTABusStops] = fetchBusstops(startId)
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
    stops = fetchBusstops(startId)
  }

  def fetchBusstops(startId: Int) : List[LTABusStops] = {
    val r = requests.get(
      "http://datamall2.mytransport.sg/ltaodataservice/BusStops?$skip=" + startId,
      headers = Map("AccountKey" -> api_key)
    )
    decode[LTABusStopsResponse](r.text).right.get.value
  }

}

object BusServicesBuilder extends LTADatabaseSession {
  def getBusServices(startId: Int = 0) = {
    val r = requests.get(
      "http://datamall2.mytransport.sg/ltaodataservice/BusRoutes?$skip=" + startId,
      headers = Map("AccountKey" -> api_key)
    )
    r.text
  }

  var finished = false
  var startId = 0
  while (!finished) {
    println("startid ", startId)
    val services = getBusServices(startId)
    val decodedServices = decode[LTABusServicesResponse](services) match {
      case Right(resp) => resp.value
      case Left(exc) => throw exc
    }

    val busServices = TableQuery[BusServiceStopTable]

    decodedServices.foreach(s => {
      val insertActions = DBIO.seq(
        busServices += s
      )

      val resp = Try({Await.result(db.run(insertActions), 5 seconds)}) match {
        case Success(s) => s
        case Failure(exc) => { // don't throw because we don't want to stop the program
          println("INSERT Failure ", exc)
          None
        }
      }
    })

    startId += decodedServices.length
    finished = decodedServices.length == 0
  }

}