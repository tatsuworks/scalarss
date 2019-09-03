package it.xaan.rss.database

import com.apple.foundationdb.tuple.{Tuple => JT}

object Tuple {
  def pack(values: Any*): Array[Byte] = JT.from(values: _*).pack()
}
