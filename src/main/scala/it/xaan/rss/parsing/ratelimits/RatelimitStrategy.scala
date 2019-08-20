package it.xaan.rss.parsing.ratelimits

import scalaj.http.HttpResponse

trait RatelimitStrategy {
  def sleep[T](response: HttpResponse[T]): Long = 100

  def allowed(url: String): Boolean = true
}
