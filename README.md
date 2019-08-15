# Scala RSS

A RSS parser that posts to discord webhooks with new posts.

### Config

```json
{
  "maxThreads": -1,
  "redisHost": "127.0.0.1",
  "redisPort": 6379,
  "fdbCluster": "/path/to/my/fdb.cluster",
  "redditUser": "MyReddituser"
}
```
**If default value is `?` then it is a required value and MUST be replaced.**

|  Key |  Description  | Default Value |
| ------------ | ------------ | ------------ |
| maxThreads  | The max amount of threads to spawn for RSS processing.   | -1. Keep in mind this will be logically equal to `Runtime.getRuntime().availableProcessors() * 2` when left at -1 |
| redisHost   | The host of the redis instance to connect to.  | 127.0.0.1 |
| redisPort   | The port of the redis instance to connect to.  | ? |
| fdbCluster   | The FDB cluster to grab info from. | ? |
| redditUser   | The name of the reddit user to put in the User-Agent. | ? |

