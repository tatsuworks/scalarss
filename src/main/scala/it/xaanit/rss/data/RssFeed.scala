package it.xaanit.rss.data

import play.api.libs.json.{Json, OFormat}


case class RssFeed(
                    identifier: String,
                    url: String,
                    tries: Int,
                    info: Seq[Info]
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
                 channel: Long,
                 guild: Long,
                 webhook: String = "",
                 includedTags: Array[String] = Array(),
                 excludedTags: Array[String] = Array()
               )

object Info {
  implicit val format: OFormat[Info] = Json.format[Info]
}