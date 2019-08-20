package it.xaan.rss.parsing

case class Story(
                  title: String,
                  category: Category,
                  url: String,
                  updated: Long,
                  description: String
                )

case class Category(
                     name: String,
                     term: String
                   ) {
  override def equals(obj: Any): Boolean = obj match {
    case other: Category => other.name == name && other.term == term
    case other: String => name == other || term == other
    case _ => false
  }
}
