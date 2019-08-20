package it.xaan.rss

import java.util.concurrent.Executors

import it.xaan.rss.data.{Config, Info}
import it.xaan.rss.database.{Foundation, Redis}
import it.xaan.rss.parsing.Reader
import scalaj.http.Http

import scala.util.{Failure, Success}

class Master(val config: Config) {
  private val pool = Executors.newFixedThreadPool(if (config.maxThreads == -1) Runtime.getRuntime.availableProcessors() * 2 else config.maxThreads)
  private val executor = Executors.newScheduledThreadPool(2)
  private val redis = new Redis(config)
  val fdb = new Foundation(config)


  def test(): Unit = {
    fdb.save(url = "https://www.reddit.com/r/TATSU_TESTING/.rss", increment = false, info = Info(channel = "570165019065450496", guild = "390426490103136256", webhook = "https://canary.discordapp.com/api/webhooks/610225631883558931/NbZGPctSGQKcfOe6z8bgbq6VGa4tpyGITn0vqPtjxODUrk46a8gxDyia_QkIIeuZ-zjF"))
    fdb.save(url = "https://www.reddit.com/r/TATSU_TESTING/.rss", increment = false, info = Info(channel = "390437567431966720", guild = "390426490103136256", webhook = "https://canary.discordapp.com/api/webhooks/610225631883558931/NbZGPctSGQKcfOe6z8bgbq6VGa4tpyGITn0vqPtjxODUrk46a8gxDyia_QkIIeuZ-zjF"))
    fdb.save(url = "https://www.reddit.com/r/TATSU_TESTING/.rss", increment = false, info = Info(channel = "534971369750921216", guild = "390426490103136256", webhook = "https://canary.discordapp.com/api/webhooks/610225631883558931/NbZGPctSGQKcfOe6z8bgbq6VGa4tpyGITn0vqPtjxODUrk46a8gxDyia_QkIIeuZ-zjF"))
    fdb.save(url = "https://www.reddit.com/r/TATSU_TESTING/.rss", increment = false, info = Info(channel = "390437583630499840", guild = "390426490103136256", webhook = "https://canary.discordapp.com/api/webhooks/610225631883558931/NbZGPctSGQKcfOe6z8bgbq6VGa4tpyGITn0vqPtjxODUrk46a8gxDyia_QkIIeuZ-zjF"))
  }

  def test2(): Unit = {
    val feeds = fdb.allFeeds ++ Seq(fdb.get("https://www.reddit.com/r/TATSU_TESTING/.rss").orNull)

    println(feeds)
    feeds.filter(_ != null).take(1).foreach(feed => {
      Reader.load(feed, config, fdb) match {
        case Failure(exception) =>
          exception.printStackTrace()
        case Success(value) =>
          Webhook.send(feed, value)
      }
    })
  }

  def start(): Unit = {}

  private def premium(): Unit = {}

  private def normal(): Unit = {}
}