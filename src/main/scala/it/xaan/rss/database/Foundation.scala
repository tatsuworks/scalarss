package it.xaan.rss.database

import java.util

import com.apple.foundationdb.directory.DirectoryLayer
import com.apple.foundationdb.tuple.{Tuple => JT}
import com.apple.foundationdb.{FDB, Transaction}
import it.xaan.rss.data.{Config, Info, RssFeed}
import play.api.libs.json._

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

/**
 * Represents a connection to FoundationDB.
 *
 * @param config The config file to read cluster info from.
 */
class Foundation(val config: Config) {
  private val fdb = FDB.selectAPIVersion(610)
  private val UrlPrefix = "rss_urls"
  private val DiscordPrefix = "rss_discord"
  private val layer = "srss"


  private def makeList(values: Seq[String]): util.ArrayList[String] = {
    val list = new util.ArrayList[String]()
    values.foreach(list.add)
    list
  }

  /**
   * Clears an RSS feed from use and deletes it from all channels/guilds that are subscribed to it.
   *
   * @param url The url of the feed to clear
   */
  def clear(url: String): Unit = execute { transaction =>
    val dir = DirectoryLayer.getDefault.createOrOpen(transaction, makeList(Seq(layer))).get()
    get(url) match {
      case None => // No feed found?
      case Some(feed) =>
        feed.info.foreach(info => clearFrom(guild = info.guild.toLong, channel = info.channel.toLong, url = url))
        transaction.clear(
          dir.pack(s"$UrlPrefix:$url")
        )
    }
  }

  /**
   * Clears a specific feed from a guild.
   *
   * @param guild   The guild to clear from.
   * @param channel The channel to clear from.
   * @param url     The url of the feed to clear.
   */
  def clearFrom(guild: Long, channel: Long, url: String): Unit = {
    val feed = get(url)
    execute { transaction =>
      val dir = DirectoryLayer.getDefault.createOrOpen(transaction, makeList(Seq(layer))).get()
      val feeds = getFeedsForChannel(guild, channel).map(_.url).filter(_ != url)
      val channelKey = dir.pack(s"$DiscordPrefix:$guild:$channel")
      val urlKey = dir.pack(s"$UrlPrefix:$url")
      if (feeds.isEmpty) {
        transaction.clear(channelKey)
      } else {
        transaction.set(
          channelKey,
          Json.toJson(feeds).toString().getBytes("utf-8")
        )
      }
      feed match {
        case None => // No feed????
        case Some(feed) =>
          val `new` = feed.copy(info = feed.info.filter(info => info.guild.toLong != guild && info.channel.toLong != channel))
          if (`new`.info.isEmpty) {
            transaction.clear(urlKey)
          } else {
            transaction.set(
              urlKey,
              Json.toJson(`new`).toString().getBytes("utf-8")
            )
          }
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
  def getFeedsForChannel(guild: Long, channel: Long): Set[RssFeed] =
    execute[Seq[String]] { transaction =>
      val dir = DirectoryLayer.getDefault.createOrOpen(transaction, makeList(Seq(layer))).get()
      val bytes = transaction.get(
        dir.pack(s"$DiscordPrefix:$guild:$channel")
      ).get()
      if (bytes == null) Seq()
      else Json.parse(bytes).validateOpt[Seq[String]].get.getOrElse(Seq())
    }
      .iterator
      .map(get)
      .filter(_.isDefined)
      .map(_.get)
      .toSet


  /**
   * Saves a feed in FDB as json with the key `rss:$guild:$channel:$name`
   *
   * @param url       The url of the feed to save.
   * @param increment If we should increment tries, or leave as is.
   * @param info      The info of the RSS subscribe
   */
  def save(url: String, increment: Boolean, info: Info): Unit = {
    val feed = get(url).map(old => old.copy(info = old.info.filter(_ != info) ++ Set(info))).getOrElse(RssFeed(url, 0, Set(info)))

    val channelFeeds = getFeedsForChannel(info.guild.toLong, info.channel.toLong).map(_.url).filter(_ != url) ++ Seq(url)
    execute(transaction => {
      val dir = DirectoryLayer.getDefault.createOrOpen(transaction, makeList(Seq(layer))).get()
      transaction.set(
        dir.pack(s"$UrlPrefix:$url"),
        Json.toJson(feed).toString().getBytes("utf-8")
      )

      transaction.set(
        dir.pack(s"$DiscordPrefix:${info.guild}:${info.channel}"),
        Json.toJson(channelFeeds).toString().getBytes("utf-8")
      )
    })
  }

  def updateChecked(url: String, guild: String): Unit = {
    val feed = get(url).map(old => old.copy(info = old.info.map(info => info.copy(lastUpdated = System.currentTimeMillis()))))
    execute { transaction =>
      feed match {
        case Some(value) =>
          val dir = DirectoryLayer.getDefault.createOrOpen(transaction, makeList(Seq(layer))).get()

          transaction.set(
            dir.pack(s"$UrlPrefix:$url"),
            Json.toJson(value).toString().getBytes("utf-8")
          )
        case None => // Ignore
      }
    }
  }

  def updateTries(url: String): Unit = {
    val feed = get(url).map(old => old.copy(tries = old.tries + 1))
    execute { transaction =>
      feed match {
        case Some(value) =>
          val dir = DirectoryLayer.getDefault.createOrOpen(transaction, makeList(Seq(layer))).get()

          transaction.set(
            dir.pack(s"$UrlPrefix:$url"),
            Json.toJson(value).toString().getBytes("utf-8")
          )
        case None => // Ignore
      }
    }
  }


  /**
   * Grabs all feeds from a list of URLs.
   *
   * @param urls The urls to look for.
   * @return A list of feeds, if they exist.
   */
  def getFeeds(urls: Set[String]): Set[RssFeed] =
    urls.iterator
      .map(get)
      .filter(_.isDefined)
      .map(_.get)
      .toSet

  /**
   * Grabs a specific RSS feed
   *
   * @param url The url of the RSS feed.
   * @return Some if the RSS feed exists, else None.
   */
  def get(url: String): Option[RssFeed] =
    execute[Option[RssFeed]](transaction => {
      val dir = DirectoryLayer.getDefault.createOrOpen(transaction, makeList(Seq(layer))).get()
      val value = transaction.get(
        dir.pack(s"$UrlPrefix:$url")
      ).get()
      if (value == null) None
      else Json.parse(value).validateOpt[RssFeed].get
    })


  def execute[T](exec: Transaction => T): T = {
    val db = fdb.open(config.fdbCluster)
    val transaction = db.createTransaction()
    val res = exec(transaction)
    Try(transaction.commit().get()) match {
      case Failure(exception) => exception.printStackTrace()
      case Success(_) => // Ignore
    }
    transaction.close()
    db.close()
    res
  }

  /**
   * Gets all feeds in a guild
   *
   * @param guild The ID of the guild
   * @return A possibly empty Seq of every feed belonging to the specified guild.
   */
  def getFeedsForGuild(guild: Long): Set[RssFeed] = getFeeds(s"$DiscordPrefix:$guild").iterator.map(get)
    .filter(_.isDefined)
    .map(_.get)
    .toSet

  private def getFeeds(key: String): Set[String] = execute[Seq[String]] { transaction =>
    val dir = DirectoryLayer.getDefault.createOrOpen(transaction, makeList(Seq(layer))).get()
    transaction.getRange(
      dir.pack(s"$key:"),
      dir.pack(s"$key;")
    ).asList().get()
      .asScala
      .map(kv => Json.parse(kv.getValue).validateOpt[Seq[String]].getOrElse(None))
      .filter(_.isDefined)
      .flatMap(_.get)
      .toSeq
  }.toSet

  /**
   * Gets all feeds
   *
   * @return A possibly empty Seq of every feed.
   */
  def allFeeds: Set[RssFeed] =
    execute { transaction =>
      val dir = DirectoryLayer.getDefault.createOrOpen(transaction, makeList(Seq(layer))).get()
      transaction.getRange(
        dir.pack(s"$UrlPrefix:"),
        dir.pack(s"$UrlPrefix;")
      ).asList().get()
        .asScala
        .map(_.getValue)
        .map(bytes => Try(Json.parse(bytes).validateOpt[RssFeed].get).getOrElse(None))
        .filter(_.isDefined)
        .map(_.get)
        .toSet
    }

  def premiumGuilds: Set[Long] =
    execute { transaction =>
      val dir = DirectoryLayer.getDefault.createOrOpen(transaction, makeList(Seq("atlas3"))).get()
      transaction.getRange(
        dir.subspace(JT.from(18)).pack(Tuple.pack("", 1)),
        dir.subspace(JT.from(18)).pack(";")
      ).asList().get()
        .asScala
        .map(kv => (kv.getKey, kv.getValue))
        .filter { case (_, time) => !expired(time) }
        .map(_._1)
        .map(bytes => JT.fromBytes(bytes).get(2).toString)
        .flatMap(_.toLongOption)
    }.toSet

  private def expired(arr: Array[Byte]): Boolean =
    arr.reverse.filter(_ != 0).foldLeft(0) { (x, y) => (x << 8) | y } >= System.currentTimeMillis()

}