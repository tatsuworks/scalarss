package it.xaan.rss

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.{WebhookEmbed, WebhookEmbedBuilder, WebhookMessageBuilder}
import it.xaan.rss.data.RssFeed
import it.xaan.rss.parsing.ParsedFeed

import scala.util.{Failure, Try}

object Webhook {
  def send(feed: RssFeed, parsed: ParsedFeed): Unit = {
    feed.info.foreach(guild => {
      parsed.stories.foreach(story => {
        if (!guild.excludedTags.exists(story.category.equals) && (guild.includedTags.isEmpty || guild.includedTags.exists(story.category.equals))) {
          val client = WebhookClient.withUrl(guild.webhook)
          val message = new WebhookMessageBuilder()
            .append(s"<${story.url}>")
            .addEmbeds(
              new WebhookEmbedBuilder()
                .setTitle(new WebhookEmbed.EmbedTitle(story.title, null))
                .setDescription(trail(story.description, 40))
                .setColor(0x249999)
                .setFooter(new WebhookEmbed.EmbedFooter(s"RSS Feed: ${feed.url}", null))
                .build()
            )
          Try(client.send(message.build()).get()) match {
            case Failure(exception) => println(s"Couldn't send message. ${exception.getLocalizedMessage}")
            case _ =>
          }
        }
      })
    })
  }

  private def trail(str: String, max: Int): String = {
    if (str.length == 0) return "No description provided."
    if (max >= str.length) return str
    if (max <= 0) return ""
    val words = str.split("\\s+")
    val buffer = new StringBuilder()
    for (word <- words) {
      if (word.length + buffer.length() > max) {
        return buffer.toString() + "..."
      }
      buffer.append(word).append(' ')
    }
    val string = buffer.result()
    string.patch(string.lastIndexOf(' '), "", 1).toString
  }
}