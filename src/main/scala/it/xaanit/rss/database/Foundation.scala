package it.xaanit.rss.database

import java.util

import com.apple.foundationdb.directory.DirectoryLayer
import com.apple.foundationdb.{FDB, Transaction}
import it.xaanit.rss.data.{Config, RSSFeed}
import play.api.libs.json.Json

import scala.jdk.CollectionConverters._

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

  def save(feed: RSSFeed, guild: Long, channel: Long): Unit =
    execute { transaction =>
      val dir = DirectoryLayer.getDefault.create(transaction, makeList(Seq("scala_rss"))).get()
      transaction.set(
        dir.pack(Tuple.pack(s"rss:$guild:$channel:${feed.name}")),
        Json.toJson(feed).toString().getBytes("utf-8")
      )
    }


  def get(guild: Long, channel: Long, name: String): Option[RSSFeed] =
    execute[Option[RSSFeed]] { transaction =>
      val dir = DirectoryLayer.getDefault.create(transaction, makeList(Seq("scala_rss"))).get()
      val value = transaction.get(
        dir.pack(Tuple.pack(s"rss:$guild:$channel:$name"))
      ).get()

      Json.parse(value).validateOpt[RSSFeed].get
    }

  def getFeedsForChannel(guild: Long, channel: Long): Seq[RSSFeed] = getFeeds(s"feed:$guild:$channel")

  def getFeedsForGuild(guild: Long): Seq[RSSFeed] = getFeeds(s"feed:$guild")

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