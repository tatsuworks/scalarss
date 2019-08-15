package it.xaan.rss

import java.util.concurrent.Executors

import it.xaan.rss.data.Config
import it.xaan.rss.database.{Foundation, Redis}

class Master(val config: Config) {
  private val pool = Executors.newFixedThreadPool(if (config.maxThreads == -1) Runtime.getRuntime.availableProcessors() * 2 else config.maxThreads)
  private val executor = Executors.newScheduledThreadPool(2)
  private val redis = new Redis(config)
  private val fdb = new Foundation(config)

  def start(): Unit = {}

  private def premium(): Unit = {}

  private def normal(): Unit = {}
}