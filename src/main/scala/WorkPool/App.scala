package WorkPool

import redis.{RedisBlockingClient, RedisClient}

/**
  * Created by earvinkayonga on 15/06/2016.
  */
object App {

  def main(args: Array[String]) {
    implicit val akkaSystem = akka.actor.ActorSystem("worker-pool")

    /*val redisClient = RedisClient(port = 32769, host = "192.168.99.100")
    val blockingRedisClient = RedisBlockingClient(port = 32769, host = "192.168.99.100")

    val queueManager = akkaSystem.actorOf(QueueManager.props(redisClient, blockingRedisClient))*/


    val redis = RedisConfig(
      port = 32769,
      host = "192.168.99.100",
      processingQueueName = "processing",
      jobQueueName = "jobs",
      errorQueueName = "error",
      numberOfWorkers = 10
    )

    val queue =  RedisQueue(redis).run

  }
}
