package it.xaan.rss.parsing

case class ParsedFeed(
                     title: String,
                     url: String,
                     stories: Set[Story]
                     )
