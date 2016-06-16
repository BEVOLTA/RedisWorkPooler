package WorkPool

import akka.actor.{ActorRef, ActorSystem, Props}
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


    val redis1 = RedisConfig(
      port = 32769,
      host = "192.168.99.100",
      processingQueueName = "processing",
      jobQueueName = "jobs",
      errorQueueName = "error",
      numberOfWorkers = 4
    )
    val redis2 = RedisConfig(
      port = 32769,
      host = "192.168.99.100",
      processingQueueName = "processing1",
      jobQueueName = "jobs1",
      errorQueueName = "error",
      numberOfWorkers = 4
    )

    val queue1 =  new ExampleRedisQueue(redis1).run
    val queue2 =  new ExampleRedisQueue(redis2).run

  }



  object ExampleCalculator{
    def props(parent: ActorRef) = Props(classOf[ExampleCalculator], parent)
  }

  class ExampleCalculator(parent: ActorRef) extends WorkerExec(parent: ActorRef) {
    override def receive = {
      case WorkPool.QueueManager.Job(job) ⇒
        log.info("TimeCalculator is working")
        Thread.sleep(1L)
        parent ! WorkPool.Worker.JobSuccess(job)
    }
  }

  object ExampleWorker{
    def props(parent: ActorRef) = Props(classOf[ExampleWorker], parent)
  }

  class ExampleWorker(parent: ActorRef) extends Worker(parent: ActorRef) {
    override val workExecutor = context.watch(context.system.actorOf(ExampleCalculator.props(self)))
  }

  /*object Queue{
    def props(parent: ActorRef) = Props(classOf[Queue], parent)
  }

  class Queue(
    redis: RedisConfig,
    redisClient: RedisClient,
    redisBlockingClient: RedisBlockingClient
  ) extends QueueManager(
    redis: RedisConfig,
    redisClient: RedisClient,
    redisBlockingClient: RedisBlockingClient
  ) {

  }*/

  class ExampleRedisQueue(override val redis: RedisConfig)(implicit master: ActorSystem)  extends RedisQueue(redis: RedisConfig){


    override def run = {
      val redisClient = RedisClient(port = redis.port, host = redis.host)
      val blockingRedisClient = RedisBlockingClient(port = redis.port, host = redis.host)
      val queue = master.actorOf(QueueManager.props(redis, redisClient, blockingRedisClient))
    }
  }


}

