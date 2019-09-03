# Scala RSS

A RSS parser that posts to discord webhooks with new posts.

### Config

```json
{
  "maxThreads": -1,
  "apiPort": 8080,
  "fdbCluster": "/path/to/my/fdb.cluster",
  "redditUser": "MyReddituser"
}
```
**If default value is `?` then it is a required value and MUST be replaced.**

|  Key |  Description  | Default Value |
| ------------ | ------------ | ------------ |
| maxThreads  | The max amount of threads to spawn for RSS processing.   | -1. Keep in mind this will be logically equal to `Runtime.getRuntime().availableProcessors() * 2` when left at -1 |
| apoPort   | The port for the API to listen to | 8080 |
| fdbCluster   | The FDB cluster to grab info from. | /etc/foundationdb/fdb.cluster |
| redditUser   | The name of the reddit user to put in the User-Agent. | ? |

