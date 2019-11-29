package it.xaan.rss.rest

import io.javalin.http.Context
import it.xaan.rss.data.{Config, Info}
import it.xaan.rss.data.rest.{RestFeed, DeleteFeed}
import it.xaan.rss.database.Foundation
import it.xaan.scalalin.UserError
import it.xaan.scalalin.rest.Route
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.util.{Failure, Success, Try}

class Feeds(val config: Config, val fdb: Foundation) extends Route[Unit]("/feeds") {
  override def get(implicit ctx: Context): Unit = {
    val id = getQuery("url")
    if (id != null) {
      fdb.getFeed(id) match {
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
        fdb.getFeedsForGuild(guild).getOrElse(Set())
          .map(old => old.copy(info = old.info.filter(info => info.guild.toLong == guild)))
      ).toString())
      return
    }
    val channel = Try(channelRaw.toLong).getOrElse(throw new UserError(code = 400, json = Map("error" -> "Guild must be a valid 64bit integer!")))


    respond(200, Json.toJson(fdb.getFeedsForChannel(guild, channel).getOrElse(Set()).map(
      old => old.copy(info = old.info.filter(info => info.guild.toLong == guild && info.channel.toLong == channel))
    )).toString())
  }

  override def post(implicit ctx: Context): Unit =
    Try {
      val feed = Json.parse(ctx.body()).validate[RestFeed].get
      if (!feed.guild.matches("[0-9]+") || !feed.channel.matches("[0-9]+")) throw new IllegalArgumentException("Channel or guild not a valid ID.")
      fdb.save(increment = false, url = feed.url, info = Info(guild = feed.guild, channel = feed.channel, webhook = feed.webhook, allowed = feed.allowed, disallowed = feed.disallowed))
    } match {
      case Failure(exception) => throw new UserError(code = 400, json = Map("error" -> s"Problem adding feed. $exception"))
      case Success(_) => respond(200, Map())
    }

  override def delete(implicit ctx: Context): Unit =
    Try {
      val feed = Json.parse(ctx.body()).validate[DeleteFeed].get
      fdb.clearFrom(feed.guild.toLong, feed.channel.toLong, feed.url)
    } match {
      case Failure(exception) => throw new UserError(code = 400, json = Map("error" -> s"Problem adding feed. $exception"))
      case Success(_) => respond(200, Map())
    }


  override def patch(implicit ctx: Context): Unit =
    Try {
      val feed = Json.parse(ctx.body()).validate[RestFeed].get
      if (!feed.guild.matches("[0-9]+") || !feed.channel.matches("[0-9]+")) throw new IllegalArgumentException("Channel or guild not a valid ID.")

    } match {
      case Failure(exception) => throw new UserError(code = 400, json = Map("error" -> s"Problem adding feed. $exception"))
      case Success(_) => respond(200, Map())
    }

}
