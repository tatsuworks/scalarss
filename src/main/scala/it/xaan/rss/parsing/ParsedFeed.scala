package it.xaan.rss.parsing

case class ParsedFeed(
                     title: String,
                     url: String,
                     stories: Seq[Story]
                     )
