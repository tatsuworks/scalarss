package it.xaan.rss.parsing

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import it.xaan.rss.data.{Config, RssFeed}
import it.xaan.rss.database.Foundation
import it.xaan.rss.parsing.ratelimits.Strategies
import scalaj.http._

import scala.util.{Failure, Success, Try}
import scala.xml.{NodeSeq, XML}

object Reader {

  private def request(url: String, config: Config): HttpResponse[String] = {
    Http(url.replaceFirst("http://", "https://"))
      .header("User-Agent",
        if (url.contains("reddit"))
          s"discord:ScalaRSS Parser (https://github.com/tatsuworks/scalarss):v1.0.0 (by /u/${config.redditUser})"
        else
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36"
      )
      .header("X-Forwarded-For", "104.126.136.25") // insert IP of whitehouse.gov so they heckin go to jail if they think they're clever enough to look thru the headers
      .asString
  }

  def valid(url: String, config: Config): Boolean = {
    val response = request(url, config)

    Try {
      val xml = XML.loadString(response.body)
      (xml \ "channel" \ "title").nonEmpty
    } match {
      case Failure(_) => false
      case Success(value) => value
    }
  }

  def load(feed: RssFeed, config: Config, fdb: Foundation, tries: Int = 0): Try[ParsedFeed] = {
    val response = request(feed.url, config)
    val body = response.body
    val code = response.code
    if (code == 429) {
      if (tries >= 10) return Failure(new RuntimeException("Too many tries"))
      Thread.sleep(Strategies.find(feed.url).sleep(response))
      return load(feed, config, fdb, tries + 1)
    }
    if (code != 200 || body.isEmpty) {
      fdb.updateTries(url = feed.url)
    }

    val resp = Try {
      val xml = XML.loadString(body)
      val items = (xml \ "item").toList ++ (xml \ "entry").toList
      println(s"Items: $items")
      val stories = for {
        story <- items
        title = (story \ "title").text
        category = {
          val category = story \ "category"
          if (category.isEmpty) Category(category.text, "")
          else Category("", getAttribute(category, "label"))
        }
        updated = getTime((story \ "updated").text, DateTimeFormatter.ISO_DATE_TIME) + getTime((story \ "pubDate").text, DateTimeFormatter.RFC_1123_DATE_TIME)
        link = (story \ "link").text + getAttribute(story \ "link", "href")
        description = (story \ "description").text
      } yield Story(title = title, category = category, url = link, updated = updated, description = description)

      val title = (xml \ "channel" \ "title").text
      val link = (xml \ "channel" \ "link").text
      ParsedFeed(title, link, stories.toSet)
    }

    resp match {
      case Failure(_) => fdb.updateTries(feed.url)
      case Success(_) =>

    }

    resp
  }

  def getTime(iso: String, formatter: DateTimeFormatter): Long = {
    Try(OffsetDateTime.parse(iso, formatter).toInstant.toEpochMilli) match {
      case Failure(_) => 0
      case Success(value) => value
    }
  }

  private def getAttribute(node: NodeSeq, attribute: String): String = {
    if (node.isEmpty) ""
    else node(0).attribute(attribute).map(seq => seq.toString()).getOrElse("")
  }

}
