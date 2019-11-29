package it.xaan.rss.data

/**
 * Represents the configuration file.
 *
 * @param maxThreads The max amount of threads to spawn.
 * @param fdbCluster The cluster of the FDB instance to use.
 * @param redditUser The name of the reddit user for the reddit URLs
 */
case class Config(
                   maxThreads: Int = -1,
                   apiPort: Int = 8080,
                   fdbCluster: String = "/etc/foundationdb/fdb.cluster",
                   redditUser: String = "",
                   webhookProxy: String = ""
                 )