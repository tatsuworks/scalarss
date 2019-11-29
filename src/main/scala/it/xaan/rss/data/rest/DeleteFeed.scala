package it.xaan.rss.data.rest

import play.api.libs.json.{Json, OFormat}

case class DeleteFeed(
                    guild: String,
                    channel: String,
                    url: String
                  )

object DeleteFeed {
  implicit val format: OFormat[DeleteFeed] = Json.format[DeleteFeed]
}