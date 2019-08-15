package it.xaan.rss.data

/**
 * Represents the configuration file.
 *
 * @param maxThreads The max amount of threads to spawn.
 * @param redisHost  The host of the redis server to use.
 * @param redisPort  The port of the redis server to use.
 * @param fdbCluster The cluster of the FDB instance to use.
 * @param redditUser The name of the reddit user for the reddit URLs
 */
case class Config(
                   maxThreads: Int = -1,
                   redisHost: String = "127.0.0.1",
                   redisPort: Int = 6379,
                   fdbCluster: String = "/etc/foundationdb/fdb.cluster",
                   redditUser: String = ""
                 )