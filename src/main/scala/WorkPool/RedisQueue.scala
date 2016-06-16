package WorkPool

import akka.actor.{ActorRef, ActorSystem}
import redis.{RedisBlockingClient, RedisClient}

/**
  * Created by earvinkayonga on 16/06/2016.
  */


case class RedisQueue(redis: RedisConfig)(implicit master: ActorSystem) {
 def run = {
   val redisClient = RedisClient(port = redis.port, host = redis.host)
   val blockingRedisClient = RedisBlockingClient(port = redis.port, host = redis.host)
   val queueManager = master.actorOf(QueueManager.props(redis, redisClient, blockingRedisClient))
 }
}
