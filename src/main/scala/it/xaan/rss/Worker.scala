package it.xaan.rss

import it.xaan.rss.data.{Config, RssFeed}
import it.xaan.rss.database.Foundation
import it.xaan.rss.parsing.Reader

import scala.util.{Failure, Success}


class Worker(val config: Config, val fdb: Foundation) {

  def work(feed: RssFeed): Unit = {
    println(s"Parsing ${feed.url}")
    val parsed = Reader.load(feed, config, fdb)
    parsed match {
      case Failure(exception) => exception.printStackTrace() // TODO: LOGGING
      case Success(value) =>
        Webhook.send(feed, value, config)
        feed.info.foreach(info => fdb.updateChecked(feed.url, info.guild))
    }
  }

}