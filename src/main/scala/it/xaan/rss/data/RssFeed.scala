package it.xaan.rss.data

import play.api.libs.json.{Json, OFormat}


case class RssFeed(
                    url: String,
                    tries: Int,
                    lastUpdated: Long,
                    info: Set[Info]
                  )


object RssFeed {
  /**
   * Represents a format of RSS feeds for json parsing. Always in scope.
   */
  implicit val format: OFormat[RssFeed] = Json.format[RssFeed]
}


/**
 * Represents an RSS feed
 *
 * @param channel      The channel that subscribed to this RSS URL
 * @param guild        The guild that subscribed to this RSS URL
 * @param webhook      The URl of the webhook to post to.
 * @param includedTags The tags to whitelist.
 * @param excludedTags The tags to blacklist.
 */
case class Info(
                 channel: String,
                 guild: String,
                 webhook: String = "",
                 includedTags: Set[String] = Set(),
                 excludedTags: Set[String] = Set()
               )

object Info {
  implicit val format: OFormat[Info] = Json.format[Info]
}