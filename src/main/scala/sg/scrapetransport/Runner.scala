package sg.scrapetransport

import akka.actor.{ActorSystem, Props}

import sg.scrapetransport.actors.BusServicesStopActor

case class BusServiceNo(no: String)

object Runner extends App {
  override def main(args: Array[String]): Unit = {
    val system = ActorSystem("PingPongSystem")
    val servicesActor = system.actorOf(Props[BusServicesStopActor], name="busServicesActor")
    // servicesActor ! "crawl"
    servicesActor ! BusServiceNo("77")
    system.stop(servicesActor)
    system.terminate
  }
}
