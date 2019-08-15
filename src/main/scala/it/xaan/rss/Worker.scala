package it.xaan.rss

import it.xaan.rss.data.{Config, RssFeed}
import it.xaan.rss.database.{Foundation, Redis}


class Worker(val config: Config, val redis: Redis, val fdb: Foundation) {


  def work(feed: RssFeed): Unit = {
  }

}