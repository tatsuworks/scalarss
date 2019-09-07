package it.xaan.rss

import better.files.File
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder._
import it.xaan.rss.data.Config
import it.xaan.rss.rest.Feeds
import it.xaan.scalalin.rest.Route
import play.api.libs.json.{Json, OFormat}

import scala.util.{Failure, Success, Try}

object Main {
  def main(args: Array[String]): Unit = {
    implicit val format: OFormat[Config] = Json.format[Config]
    val cfg = File("./config.json")
    cfg.createFileIfNotExists()
    val config = Try(Json.parse(cfg.byteArray).validate[Config].get) match {
      case Failure(_) =>
        cfg.write(Json.toJson(Config()).toString())
        println("Please fill in config file!")
        return
      case Success(value) => value
    }
    val master = new Master(config)
    master.start()
    val routes = Array[Route[Unit]](new Feeds(master.config, master.fdb))
    Javalin.create { _ =>

    }.routes(
      () => {
        routes.foreach { route =>
          path(route.path, () => {
            get(route.call)
            post(route.call)
            patch(route.call)
            delete(route.call)
            put(route.call)
            head(route.call)
          })
        }
      }
    ).start(master.config.apiPort)
  }
}
