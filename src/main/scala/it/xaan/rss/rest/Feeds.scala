package it.xaan.rss.rest

import java.util

import com.apple.foundationdb.directory.DirectoryLayer
import io.javalin.http.Context
import it.xaan.rss.data.Config
import it.xaan.rss.database.Foundation
import it.xaan.scalalin.UserError
import it.xaan.scalalin.rest.Route
import play.api.libs.json.Json

import scala.util.Try

class Feeds(val config: Config, val fdb: Foundation) extends Route[Unit]("/feeds") {
  override def get(implicit ctx: Context): Unit = {
    val id = getQuery("url")
    if (id != null) {
      fdb.get(id) match {
        case Some(feed) =>
          respond(200, Json.toJson(feed).toString())
        case None => respondMap(400, Map("error" -> "Invalid feed identifier."))
      }
      return
    }

    val guildRaw = getQuery("guild")
    val channelRaw = getQuery("channel")
    if (guildRaw == null && channelRaw == null) {
      respond(400, Map("error" -> "id, guild, or channel must exist."))
      return
    }

    if (guildRaw == null && channelRaw != null) {
      respond(400, Map("error" -> "Guild MUST exist if channel exists."))
      return
    }

    val guild = Try(guildRaw.toLong).getOrElse(throw new UserError(code = 400, json = Map("error" -> "Guild must be a valid 64bit integer!")))

    if (channelRaw == null) {
      println(fdb.getFeedsForGuild(guild))
      respond(200, Json.toJson(
        fdb.getFeedsForGuild(guild)
          .map(old => old.copy(info = old.info.filter(info => info.guild.toLong == guild)))
      ).toString())
      return
    }
    val channel = Try(channelRaw.toLong).getOrElse(throw new UserError(code = 400, json = Map("error" -> "Guild must be a valid 64bit integer!")))


    respond(200, Json.toJson(fdb.getFeedsForChannel(guild, channel).map(
      old => old.copy(info = old.info.filter(info => info.guild.toLong == guild && info.channel.toLong == channel))
    )).toString())

  }
}
