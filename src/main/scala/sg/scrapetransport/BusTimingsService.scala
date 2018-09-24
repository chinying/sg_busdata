package sg.scrapetransport

import com.typesafe.config._

import io.circe._
import io.circe.parser._
import io.circe.generic.semiauto._
import requests._
import scala.concurrent.{Await, Future}

import slick.jdbc.JdbcBackend.Database
import slick.driver.PostgresDriver.api._
import slick.dbio.{DBIO, NoStream}
import slick.dbio.Effect.Read

import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}

// all are set as strings
case class NextBusJson(
  OriginCode: String, DestinationCode: String, EstimatedArrival: String,
  Latitude: String, Longitude: String, VisitNumber: String,
  Load: String, Feature: String, Type: String
)

// optionals are for if entire json object is removed
case class LTAServicesJson(
  ServiceNo: String, Operator: String,
  NextBus: Option[NextBusJson], NextBus2: Option[NextBusJson], NextBus3: Option[NextBusJson]
)
case class LTABusStopResponse(`odata.metadata`: String, `BusStopCode`: String, `Services`: List[LTAServicesJson])

case class NextBus(
  originCode: String, destinationCode: String, estimatedArrival: String, // change to time
  latitude: Double, longitude: Double, visitNumber: Int,
  load: String, feature: String, _type: String
) {
  def nextBusFromJson (c: NextBusJson) : NextBus = {
    val latitude = Try(c.Latitude.toDouble) match {
      case Success(s) => s
      case Failure(_) => -999
    }

    val longitude = Try(c.Longitude.toDouble) match {
      case Success(s) => s
      case Failure(_) => -999
    }

    val visitNumber = Try(c.VisitNumber.toInt) match {
      case Success(s) => s
      case Failure(_) => -999
    }

    NextBus(
      c.OriginCode, c.DestinationCode, c.EstimatedArrival,
      latitude, longitude, visitNumber,
      c.Load, c.Feature, c.Type
    )
  }
}


object BusTimingsService extends LTADatabaseSession {

  implicit val nextBusDecoder: Decoder[NextBusJson] = deriveDecoder[NextBusJson]
  implicit val ltaServicesDecoder: Decoder[LTAServicesJson] = deriveDecoder[LTAServicesJson]
  implicit val ltaBusStopResponseDecoder: Decoder[LTABusStopResponse] = deriveDecoder[LTABusStopResponse]


  implicit val nextBusEncoder: Encoder[NextBusJson] = deriveEncoder[NextBusJson]
  implicit val ltaServicesEncoder: Encoder[LTAServicesJson] = deriveEncoder[LTAServicesJson]
  implicit val ltaBusStopResponseEncoder: Encoder[LTABusStopResponse] = deriveEncoder[LTABusStopResponse]


  def getArrivalTimings(busStopCode: String) = {
    val r = requests.get(
      "http://datamall2.mytransport.sg/ltaodataservice/BusArrivalv2?BusStopCode=" + busStopCode,
      headers = Map("AccountKey" -> api_key)
    )

    val responseJson = parse(r.text) match {
      case Right(r) => r
      case Left(exc) => {
        println("encoding failed")
        throw exc
      }
    }


    val arrivals = responseJson.as[LTABusStopResponse] match {
      case Right(r) => r.Services
      case Left(exc) => {
        println("decoding failed", exc)
        throw exc
      }
    }
    println("Arrivals:", arrivals)
  }

  case class LTATravelInfo(direction: Int, stopSequence: Int, stopCode: String)

  def findStopsFromService(service: String) : List[LTABusServiceStop] = {
    lazy val busServicesAtStop = TableQuery[BusServiceStopTable]
    val query: DBIOAction[Seq[LTABusServiceStop], NoStream, Read] = busServicesAtStop
      .filter(_.serviceNo === service)
      .result
    val queryResult: Future[Seq[LTABusServiceStop]] = db.run(query)

    val stops = Try({Await.result(queryResult, 10 seconds)}) match {
      case Success(s) => s
      case Failure(ex) => {
        println("SELECT Failure ", ex)
        Vector()
      }
    }
    stops.toList
  }

}
