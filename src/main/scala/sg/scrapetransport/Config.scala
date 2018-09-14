package sg.scrapetransport

import com.typesafe.config._

object AppConfig {
  val conf = ConfigFactory.load("config.conf")
}