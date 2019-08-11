package it.xaanit.rss.data

case class Config(
                  maxThreads: Int = -1,
                  redisHost: String = "127.0.0.1",
                  redisPort: Int = -1,
                  fdbCluster: String = "AHHHHHHHHHHHHHH"
                 )