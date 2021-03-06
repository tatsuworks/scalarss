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
| webhookProxy | The proxy to send webhooks through. For instance if you wanted to send it to https://mywebsite.com/api/webhooks/$ID/$TOKEN, then you'd set this value to mywebsite.com/api/webhooks/ | ?


# Endpoints

**GET** /feeds

Query params:
```
+---------+-----------------------------+
|  Name   |         Description         |
+---------+-----------------------------+
| guild   | The ID of the guild.        |
| channel | The ID of the channel.      |
| url     | The URL of the feed.        |
+---------+-----------------------------+
```

Notes:
- Guild MUST exist if channel does
- If ID exists, guild and channel will be ignored.

----------------

**POST** /feeds

Body:
```
+------------+------------------------------------+---------------+
|    Name    |            Description             |     Type      |
+------------+------------------------------------+---------------+
| guild      | The ID of the guild.               | String        |
| channel    | The ID of the channel.             | String        |
| url        | The URL of the RSS feed.           | String        |
| webhook    | The URL of the webhook to post to. | String        |
| allowed    | The allowed tags.                  | Array[String] |
| disallowed | The disallowed tags.               | Array[String] |
+------------+------------------------------------+---------------+
```

**DELETE** /feeds

Body:
```
+---------+-----------------------------+
|  Name   |         Description         |
+---------+-----------------------------+
| guild   | The ID of the guild.        |
| channel | The ID of the channel.      |
| url     | The URL of the feed.        |
+---------+-----------------------------+
```

Notes:

- Every parameter is required.

-------------------


**PATCH** /feeds

Body:
```
+------------+------------------------------------+---------------+
|    Name    |            Description             |     Type      |
+------------+------------------------------------+---------------+
| guild      | The ID of the guild.               | String        |
| channel    | The ID of the channel.             | String        |
| url        | The URL of the RSS feed.           | String        |
| allowed    | The allowed tags.                  | Array[String] |
| disallowed | The disallowed tags.               | Array[String] |
+------------+------------------------------------+---------------+
```

Notes:

- Every field is required.
