package it.xaan.rss.data

import play.api.libs.json.{Json, OFormat}

case class WebhookMessage(
                         avatar_url: String,
                         username: String,
                         content: String,
                         embeds: Seq[WebhookEmbed]
                         ) {
  implicit val ef: OFormat[EmbedFooter] = Json.format[EmbedFooter]
  implicit val we: OFormat[WebhookEmbed] = Json.format[WebhookEmbed]
  implicit val wm: OFormat[WebhookMessage] = Json.format[WebhookMessage]
  def toJson: String = Json.toJson(this).toString()
}


case class WebhookEmbed(
                       title: String,
                       description: String,
                       color: Int,
                       footer: EmbedFooter
                       )

case class EmbedFooter(
                      text: String
                      )
