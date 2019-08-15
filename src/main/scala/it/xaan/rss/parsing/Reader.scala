package it.xaan.rss.parsing

import it.xaan.rss.data.{Config, RssFeed}
import it.xaan.rss.database.Foundation
import it.xaan.rss.parsing.ratelimits.Strategies
import scalaj.http._

import scala.util.Try
import scala.xml.XML

object Reader {

  def load(feed: RssFeed, config: Config, fdb: Foundation, tries: Int = 0): Option[ParsedFeed] = {
    val response = Http(feed.url.replaceFirst("http", "https"))
      .header("User-Agent",
        if (feed.url.contains("reddit"))
          s"discord:ScalaRSS Parser (https://github.com/tatsuworks/scalarss):v1.0.0 (by /u/${config.redditUser})"
        else
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36"
      )
      .header("X-Forwarded-For", "104.126.136.25") // insert IP of whitehouse.gov so they heckin go to jail if they think they're clever enough to look thru the headers
      .asString
    val body = response.body
    val code = response.code
    if (code == 429) {
      if (tries >= 10) return None
      Thread.sleep(Strategies.find(feed.url).sleep(response))
      return load(feed, config, fdb, tries + 1)
    }
    if (code != 200 || body.isEmpty) {
      fdb.updateTries(identifier = feed.identifier)
    }

    Try {
      val xml = XML.loadString(body)
      val items = xml \ "items"
      val stories = for {
        story <- items
        title = (story \ "title").text
        link = (story \ "link").text
        description = (story \ "description").text
      } yield Story(title, link, description)

      val title = (xml \ "channel" \ "title").text
      val link = (xml \ "channel" \ "link").text
      ParsedFeed(title, link, stories)
    }.toOption
  }

}
