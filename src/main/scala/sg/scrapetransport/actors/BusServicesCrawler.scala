package sg.scrapetransport.actors

import akka.actor.Actor

import sg.scrapetransport.{BusStopsBuilder, BusServicesBuilder, BusServiceNo, BusTimingsService}

class BusServicesStopActor extends Actor {
  def receive = {
    case "hello" => println("hello back at you")

    case "init" => {
      // TODO: clean this up
      val (a, b) = (BusStopsBuilder, BusServicesBuilder)
    }

    case bus: BusServiceNo => {
      val stops = BusTimingsService.findStopsFromService(bus.no)
      println(s"${stops.length}, stops found for service ${bus.no}")
      stops.par.foreach(stop => {
        BusTimingsService.getArrivalTimings(stop.BusStopCode)
      })
    }

    case _       => println("huh?")
  }
}