package it.xaan.rss.database

import com.apple.foundationdb.tuple.{Tuple => JT}

object Tuple {
  def pack(values: String*): Array[Byte] = JT.from(values: _*).pack()
}
