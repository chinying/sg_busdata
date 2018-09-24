package sg.scrapetransport

import requests._

case class LTABusStops(BusStopCode: String, RoadName: String, Description: String, Latitude: Double, Longitude: Double)
case class LTABusStopsResponse(`odata.metadata`: String, value: List[LTABusStops])


trait BusAtStop {
  val service: String
  val busStop: String

  // def withPerson(time: Double) = new BusServiceStop(this, time)
}

case class LTABusServiceStop (
  ServiceNo: String, Operator: String, Direction: Int, StopSequence: Int,
  BusStopCode: String, Distance: Double,
  WD_FirstBus: String, WD_LastBus: String,
  SAT_FirstBus: String, SAT_LastBus: String,
  SUN_FirstBus: String, SUN_LastBus: String
) extends BusAtStop {
  val service = ServiceNo
  val busStop = BusStopCode
}

case class BusServiceStop () extends BusAtStop {
  // def
  val service = ???
  val busStop = ???
}

case class LTABusServicesResponse(`odata.metadata`: String, value: List[LTABusServiceStop])
