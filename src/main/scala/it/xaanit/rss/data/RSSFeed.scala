package it.xaanit.rss.data

import play.api.libs.json.{Json, OFormat}

/**
 * Represents an RSS feed
 *
 * @param name         The name assigned by the user.
 * @param url          The URL of the RSS feed.
 * @param webhook      The URl of the webhook to post to.
 * @param includedTags The tags to whitelist.
 * @param excludedTags The tags to blacklist.
 */
case class RSSFeed(
                    name: String,
                    url: String,
                    webhook: String,
                    includedTags: Seq[String],
                    excludedTags: Seq[String]
                  )

object RSSFeed {
  /**
   * Represents a format of RSS feeds for json parsing. Always in scope.
   */
  implicit val format: OFormat[RSSFeed] = Json.format[RSSFeed]
}
