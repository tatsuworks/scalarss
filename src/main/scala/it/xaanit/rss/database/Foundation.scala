package it.xaanit.rss.database

import java.util

import com.apple.foundationdb.directory.DirectoryLayer
import com.apple.foundationdb.{FDB, Transaction}
import it.xaanit.rss.data.{Config, RSSFeed}
import play.api.libs.json.Json

import scala.jdk.CollectionConverters._

/**
 * Represents a connection to FoundationDB.
 *
 * @param config The config file to read cluster info from.
 */
class Foundation(val config: Config) {
  private val fdb = FDB.selectAPIVersion(610)

  private def makeList(values: Seq[String]): util.ArrayList[String] = {
    val list = new util.ArrayList[String]()
    values.foreach(list.add)
    list
  }

  private def execute[T](exec: Transaction => T): T = {
    val db = fdb.open(config.fdbCluster)
    val res = db.run[T] { tr: Transaction => exec(tr)}
    db.close()
    res
  }

  /**
   * Saves a feed in FDB as json with the key `rss:$guild:$channel:$name`
   *
   * @param feed The feed to save.
   * @param guild The ID of the guild.
   * @param channel The ID of the channel.
   */
  def save(feed: RSSFeed, guild: Long, channel: Long): Unit =
    execute { transaction =>
      val dir = DirectoryLayer.getDefault.create(transaction, makeList(Seq("scala_rss"))).get()
      transaction.set(
        dir.pack(Tuple.pack(s"rss:$guild:$channel:${feed.name}")),
        Json.toJson(feed).toString().getBytes("utf-8")
      )
    }


  /**
   * Grabs a specific RSS feed
   *
   * @param guild The ID of the guild.
   * @param channel The ID of the channel.
   * @param name The nickname of the channel.
   * @return Some if the RSS feed exists, else None.
   */
  def get(guild: Long, channel: Long, name: String): Option[RSSFeed] =
    execute[Option[RSSFeed]] { transaction =>
      val dir = DirectoryLayer.getDefault.create(transaction, makeList(Seq("scala_rss"))).get()
      val value = transaction.get(
        dir.pack(Tuple.pack(s"rss:$guild:$channel:$name"))
      ).get()

      Json.parse(value).validateOpt[RSSFeed].get
    }

  /**
   * Gets all feeds in a channel
   *
   * @param guild The ID of the guild
   * @param channel the ID of the channel
   * @return A possibly empty Seq of every feed belonging to the specified channel.
   */
  def getFeedsForChannel(guild: Long, channel: Long): Seq[RSSFeed] = getFeeds(s"feed:$guild:$channel")


  /**
   * Gets all feeds in a guild
   *
   * @param guild The ID of the guild
   * @return A possibly empty Seq of every feed belonging to the specified guild.
   */
  def getFeedsForGuild(guild: Long): Seq[RSSFeed] = getFeeds(s"feed:$guild")

  /**
   * Gets all feeds
   *
   * @return A possibly empty Seq of every feed.
   */
  def allFeeds: Seq[RSSFeed] = getFeeds("rss")

  private def getFeeds(key: String): Seq[RSSFeed] = execute[Seq[RSSFeed]] { transaction =>
    val dir = DirectoryLayer.getDefault.create(transaction, makeList(Seq("scala_rss"))).get()
    transaction.getRange(
      dir.pack(Tuple.pack(s"$key:")),
      dir.pack(Tuple.pack(s"$key;"))
    ).asList().get()
      .asScala
      .map(kv => Json.parse(kv.getValue).validateOpt[RSSFeed].get)
      .filter(_.isDefined)
      .map(_.get)
      .toSeq
  }

}