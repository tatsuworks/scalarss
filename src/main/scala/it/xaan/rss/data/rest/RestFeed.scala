package it.xaan.rss.data.rest

import play.api.libs.json.{Json, OFormat}

case class RestFeed(
                    guild: String,
                    channel: String,
                    url: String,
                    webhook: String,
                    allowed: Set[String],
                    disallowed: Set[String]
                  )

object RestFeed {
  implicit val format: OFormat[RestFeed] = Json.format[RestFeed]
}