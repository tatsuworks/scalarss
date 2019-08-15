package it.xaan.rss.parsing.ratelimits

import scalaj.http.HttpResponse

object Strategies {

  case object Reddit extends RatelimitStrategy {
    override def sleep[T](response: HttpResponse[T]): Long = response.header("X-Ratelimit-Reset").map(_ * 1000).map(_.toLong).getOrElse(100)

    override def allowed(url: String): Boolean = url.contains("reddit.com")
  }
  case object Default extends RatelimitStrategy

  private val lookup: Array[RatelimitStrategy] = Array(Reddit)

  def find(url: String): RatelimitStrategy = lookup.find(_.allowed(url)).getOrElse(Default)
}
