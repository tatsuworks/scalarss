package it.xaan.rss.rest

import io.javalin.http.Context
import it.xaan.scalalin.rest.Route

object Feeds extends Route[Unit]("/feeds") {
  override def get(ctx: Context): Unit = super.get(ctx)
}
