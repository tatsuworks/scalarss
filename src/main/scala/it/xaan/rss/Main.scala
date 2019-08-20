package it.xaan.rss

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder._
import it.xaan.rss.data.Config
import it.xaan.rss.rest.Feeds
import it.xaan.scalalin.rest.Route

object Main {
  def main(args: Array[String]): Unit = {
    val master = new Master(Config())
    master.test2()
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
    ).start(8080)
  }
}
