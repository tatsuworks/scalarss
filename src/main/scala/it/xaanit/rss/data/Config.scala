package it.xaanit.rss.data

/**
 * Represents the configuration file.
 *
 * @param maxThreads The max amount of threads to spawn.
 * @param redisHost  The host of the redis server to use.
 * @param redisPort  The port of the redis server to use.
 * @param fdbCluster The cluster of the FDB instance to use.
 */
case class Config(
                   maxThreads: Int = -1,
                   redisHost: String = "127.0.0.1",
                   redisPort: Int = -1,
                   fdbCluster: String = "AHHHHHHHHHHHHHH"
                 )