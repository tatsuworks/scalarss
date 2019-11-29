package it.xaan.rss

import java.util.regex.Pattern

import it.xaan.rss.data.{Config, EmbedFooter, RssFeed, WebhookEmbed, WebhookMessage}
import it.xaan.rss.parsing.ParsedFeed
import scalaj.http.Http

object Webhook {
  val pattern = Pattern.compile("(?:https?://)?(?:\\w+\\.)?discordapp\\.com/api(?:/v\\d+)?/webhooks/(\\d+)/([\\w-]+)(?:/(?:\\w+)?)?")

  def send(feed: RssFeed, parsed: ParsedFeed, config: Config): Unit = {
    feed.info.foreach(guild => {
      parsed.stories.foreach(story => {
        if (!guild.disallowed.exists(story.category.equals) && (guild.allowed.isEmpty || guild.allowed.exists(story.category.equals))) {
          if (guild.lastUpdated < story.updated) {
            val matched = pattern.matcher(guild.webhook)
            matched.matches()
            val id = matched.group(1)
            val token = matched.group(2)
            val message = WebhookMessage(
              avatar_url = "https://cdn.discordapp.com/attachments/518914613010497538/613217485264781312/8ca21ef535d1f1ce25c4e8f8446ccbff.jpg",
              content = s"<${story.url}>",
              username = "Tatsumaki RSS",
              embeds = Seq(
                WebhookEmbed(
                  title = trail(story.title, 240),
                  description = trail(story.description, 40),
                  color = 0x249999,
                  footer = EmbedFooter(text = "RSS Feed: ${feed.url}")
                )
              )
            ).toJson

            Http(s"https://${config.webhookProxy}$id/$token")
              .postData(message)
              .headers(("Content-Type", "application/json"))
              .asString
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
