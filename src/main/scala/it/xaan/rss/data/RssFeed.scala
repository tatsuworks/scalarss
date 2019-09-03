package it.xaan.rss.data

import play.api.libs.json.{Json, OFormat}


case class RssFeed(
                    url: String,
                    tries: Int,
                    info: Set[Info]
                  ) {
  override def toString: String =
    s"""
      |RssFeed[
      |   url=$url,
      |   tries=$tries,
      |   info=$info
      |]
      |""".stripMargin
}


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
                 lastUpdated: Long = 0,
                 includedTags: Set[String] = Set(),
                 excludedTags: Set[String] = Set()
               ) {

  override def toString: String =
    s"""
      |Info[
      |   channel=$channel,
      |   guild=$guild,
      |   webhook=$webhook,
      |   lastUpdated=$lastUpdated
      |   includedTags=$includedTags,
      |   excludedTags=$excludedTags
      |]
      |""".stripMargin
}

object Info {
  implicit val format: OFormat[Info] = Json.format[Info]
}