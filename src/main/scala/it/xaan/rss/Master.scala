package it.xaan.rss

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{Executors, TimeUnit}

import it.xaan.rss.data.{Config, Info, RssFeed}
import it.xaan.rss.database.Foundation

class Master(val config: Config) {
  private val pool = Executors.newFixedThreadPool(if (config.maxThreads == -1) Runtime.getRuntime.availableProcessors() * 2 else config.maxThreads)
  private val executor = Executors.newSingleThreadScheduledExecutor()
  private val premium = new AtomicBoolean(true)
  val fdb = new Foundation(config)

  def start(): Unit =
    executor.scheduleAtFixedRate(() => {
      val all = fdb.allFeeds
      if (premium.get()) {
        val premiumGuilds = fdb.premiumGuilds
        val feeds = all // All feeds
          .filter(feed =>
            feed.info.exists( // Filter to only the ones that match this filter
              info => premiumGuilds.contains(info.guild.toLong) // Only guilds that are in the "premium guild" set
            )
          ) // filtering is generally a less expensive operation so we do it first
          .map(old => // Map all feeds to only feeds that contain premium guilds
            old.copy(info = old.info.filter(info => //filter out any non premium guilds, so it only posts there
              premiumGuilds.contains(info.guild.toLong) // Only guilds that are in the "premium" guild set
            ))
          )
        premium.set(false)
        execute(feeds)
      } else {
        premium.set(true)
        execute(all)
      }
    }, 0, 15, TimeUnit.MINUTES)

  private def execute(feeds: Set[RssFeed]): Unit =
    feeds.foreach(feed => pool.submit[Unit](() => new Worker(config, fdb).work(feed)))
}