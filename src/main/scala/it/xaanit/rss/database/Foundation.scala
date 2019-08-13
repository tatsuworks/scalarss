package it.xaanit.rss.database

import java.util

import com.apple.foundationdb.directory.DirectoryLayer
import com.apple.foundationdb.{FDB, Transaction}
import it.xaanit.rss.data.{Config, Info, RssFeed}
import play.api.libs.json.{Json, Reads}

import scala.jdk.CollectionConverters._

/**
 * Represents a connection to FoundationDB.
 *
 * @param config The config file to read cluster info from.
 */
class Foundation(val config: Config) {
  private val fdb = FDB.selectAPIVersion(610)
  private val UrlPrefix = "rss_urls"
  private val DiscordPrefix = "rss_discord"
  implicit val seq: Reads[Seq[String]] = Json.reads[Seq[String]]

  private def makeList(values: Seq[String]): util.ArrayList[String] = {
    val list = new util.ArrayList[String]()
    values.foreach(list.add)
    list
  }

  /**
   * Clears an RSS feed from use and deletes it from all channels/guilds that are subscribed to it.
   *
   * @param identifier The identifier of the
   */
  def clear(identifier: String): Unit = execute { transaction =>
    val dir = DirectoryLayer.getDefault.create(transaction, makeList(Seq("scala_rss"))).get()
    get(identifier) match {
      case None => // No feed found?
      case Some(feed) =>
        feed.info.foreach(info => clearFrom(guild = info.guild, channel = info.channel, identifier = identifier))
        transaction.clear(
          dir.pack(Tuple.pack(s"$UrlPrefix:$identifier"))
        )
    }
  }

  /**
   * Clears a specific feed from a guild.
   *
   * @param guild      The guild to clear from.
   * @param channel    The channel to clear from.
   * @param identifier The identifier of the feed to clear.
   */
  def clearFrom(guild: Long, channel: Long, identifier: String): Unit = execute { transaction =>
    val dir = DirectoryLayer.getDefault.create(transaction, makeList(Seq("scala_rss"))).get()
    val feeds = getFeedsForChannel(guild, channel).map(_.identifier).filter(_ != identifier)
    if (feeds.isEmpty) {
      transaction.clear(
        dir.pack(Tuple.pack(s"$DiscordPrefix:$guild:$channel"))
      )
    } else {
      transaction.set(
        dir.pack(Tuple.pack(s"$DiscordPrefix:$guild:$channel")),
        Json.toJson(feeds).toString().getBytes("utf-8")
      )
    }

    get(identifier) match {
      case None => // No feed????
      case Some(feed) =>
        val `new` = RssFeed(feed.identifier, feed.url, feed.tries, feed.info.filter(info => info.guild == guild && info.channel == channel))
        if (`new`.info.isEmpty) {
          transaction.clear(
            dir.pack(Tuple.pack(s"$UrlPrefix:$identifier"))
          )
        } else {
          transaction.set(
            dir.pack(Tuple.pack(s"$UrlPrefix:$identifier")),
            Json.toJson(`new`).toString().getBytes("utf-8")
          )
        }
    }
  }

  /**
   * Gets all feeds in a channel
   *
   * @param guild   The ID of the guild
   * @param channel the ID of the channel
   * @return A possibly empty Seq of every feed belonging to the specified channel.
   */
  def getFeedsForChannel(guild: Long, channel: Long): Seq[RssFeed] =
    getFeeds(s"$DiscordPrefix:$guild:$channel").iterator.map(get)
      .filter(_.isDefined)
      .map(_.get)
      .toSeq

  /**
   * Saves a feed in FDB as json with the key `rss:$guild:$channel:$name`
   *
   * @param url        The url of the feed to save.
   * @param increment  If we should increment tries, or leave as is.
   * @param identifier The identifier of the
   * @param info       The info of the RSS subscribe
   */
  def save(url: String, increment: Boolean, identifier: String, info: Info): Unit =
    execute { transaction =>
      val dir = DirectoryLayer.getDefault.create(transaction, makeList(Seq("scala_rss"))).get()

      val feeds = get(identifier).map(old => RssFeed(
        identifier = identifier, url = old.url, tries = if (increment) old.tries + 1 else 0, info = old.info ++ Seq(info)
      )).getOrElse(RssFeed(identifier, url, 0, Seq(info)))

      transaction.set(
        dir.pack(Tuple.pack(s"$UrlPrefix:$identifier")),
        Json.toJson(feeds).toString().getBytes("utf-8")
      )

      val channelFeeds = getFeedsForChannel(info.guild, info.channel).map(_.identifier) ++ Seq(identifier)
      transaction.set(
        dir.pack(Tuple.pack(s"$DiscordPrefix:${info.guild}:${info.channel}")),
        Json.toJson(channelFeeds).toString().getBytes("utf-8")
      )

    }


  /**
   * Grabs all feeds from a list of identifiers
   *
   * @param identifiers The identifiers to look for.
   * @return A list of identifiers, if they exist.
   */
  def getFeeds(identifiers: Seq[String]): Seq[RssFeed] =
    identifiers.iterator
      .map(get)
      .filter(_.isDefined)
      .map(_.get)
      .toSeq

  /**
   * Grabs a specific RSS feed
   *
   * @param identifier The identifier of the RSS feed.
   * @return Some if the RSS feed exists, else None.
   */
  def get(identifier: String): Option[RssFeed] =
    execute[Option[RssFeed]] { transaction =>
      val dir = DirectoryLayer.getDefault.create(transaction, makeList(Seq("scala_rss"))).get()
      val value = transaction.get(
        dir.pack(Tuple.pack(s"$UrlPrefix:$identifier"))
      ).get()

      Json.parse(value).validateOpt[RssFeed].get
    }

  private def execute[T](exec: Transaction => T): T = {
    val db = fdb.open(config.fdbCluster)
    val res = db.run[T] { tr: Transaction => exec(tr) }
    db.close()
    res
  }

  /**
   * Gets all feeds in a guild
   *
   * @param guild The ID of the guild
   * @return A possibly empty Seq of every feed belonging to the specified guild.
   */
  def getFeedsForGuild(guild: Long): Seq[RssFeed] = getFeeds(s"$DiscordPrefix:$guild").iterator.map(get)
    .filter(_.isDefined)
    .map(_.get)
    .toSeq

  private def getFeeds(key: String): Seq[String] = execute[Seq[String]] { transaction =>
    val dir = DirectoryLayer.getDefault.create(transaction, makeList(Seq("scala_rss"))).get()
    transaction.getRange(
      dir.pack(Tuple.pack(s"$key:")),
      dir.pack(Tuple.pack(s"$key;"))
    ).asList().get()
      .asScala
      .map(kv => Json.parse(kv.getValue).validateOpt[Seq[String]].get)
      .filter(_.isDefined)
      .flatMap(_.get)
      .toSeq
  }

  /**
   * Gets all feeds
   *
   * @return A possibly empty Seq of every feed.
   */
  def allFeeds: Seq[RssFeed] = getFeeds(s"$UrlPrefix").iterator.map(get)
    .filter(_.isDefined)
    .map(_.get)
    .toSeq

}