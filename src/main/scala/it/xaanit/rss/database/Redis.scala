package it.xaanit.rss.database

import com.redis.{RedisClient, RedisClientPool}
import it.xaanit.rss.data.{Config, RSSFeed}
import play.api.libs.json.Json

class Redis(val config: Config) {
  private val redis = new RedisClientPool(host = config.redisHost, port = config.redisPort)

  private def execute[T](exec: RedisClient => T): T = exec(redis.pool.borrowObject())

  def push(feeds: Seq[RSSFeed]): Int =
    feeds.map(feed => execute[Option[Long]] { client => client.lpush("rss_feeds", Json.toJson(feed)) }).count(_.isDefined)

  def pop(): Option[RSSFeed] =
    execute[Option[RSSFeed]](_.blpop(3, "rss_feeds").map(_._2).flatMap(Json.parse(_).validate[RSSFeed].asOpt))

}
