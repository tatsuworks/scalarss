package it.xaan.rss.database

import com.redis.{RedisClient, RedisClientPool}
import it.xaan.rss.data.{Config, RssFeed}
import play.api.libs.json.Json

/**
 *  Represents a redis instance.
 *
 * @param config The config to pull host and port data from.
 */
class Redis(val config: Config) {
  private val redis = new RedisClientPool(host = config.redisHost, port = config.redisPort)

  private def execute[T](exec: RedisClient => T): T = exec(redis.pool.borrowObject())

  /**
   * Pushes feeds to the channel.
   *
   * @param feeds The feeds to push
   * @return Amount of feeds pushed properly.
   */
  def push(feeds: Seq[RssFeed]): Int =
    feeds.map(feed => execute[Option[Long]] { client => client.lpush("rss_feeds", Json.toJson(feed)) }).count(_.isDefined)

  def pop(): Option[RssFeed] =
    execute[Option[RssFeed]](_.blpop(3, "rss_feeds").map(_._2).flatMap(Json.parse(_).validate[RssFeed].asOpt))

}
