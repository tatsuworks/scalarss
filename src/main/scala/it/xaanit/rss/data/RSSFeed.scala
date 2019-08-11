package it.xaanit.rss.data

import play.api.libs.json.{Json, OFormat}

case class RSSFeed(
                  name: String,
                    url: String,
                    webhook: String,
                    includedTags: Seq[String],
                    excludedTags: Seq[String]
                  )

object RSSFeed {
  implicit val format: OFormat[RSSFeed] = Json.format[RSSFeed]
}
