package WorkPool


import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.{ActorRefRoutee, Broadcast, RoundRobinRoutingLogic, Router}
import akka.util.Timeout
import redis.{RedisBlockingClient, RedisClient}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/*object redisQueue {
  val processingQueueName = "processing"
  val jobQueueName =  "jobs"
  val errorQueueName= "errors"
}*/


object QueueManager {

  def props(redis: RedisConfig, redisClient: RedisClient, redisBlockingClient: RedisBlockingClient): Props = {
    Props(classOf[QueueManager], redis, redisClient, redisBlockingClient)
  }

  sealed trait Message
  case class NeedWork(actor: ActorRef) extends Message
  case class Remove(Job: String) extends Message
  case class Removed(Job: String) extends Message
  case class JobFailure(Job: String, Error: String) extends Message
  case class JobDenied(Job: String) extends Message
  case class Job(job: String) extends Message
  case object NoMoreJobs extends Message
  case object HasWork extends Message
  case object CheckHasJob extends Message
}


class QueueManager(redisConf: RedisConfig, redis: RedisClient, blockingClient: RedisBlockingClient) extends Actor with ActorLogging{
  import WorkPool.QueueManager._
  import context.dispatcher


  implicit val timeout = Timeout(5 seconds)

  // Router for managing child worker actors
  var router = {
    val routees = Vector.fill(redisConf.numberOfWorkers) {
      val routee = context.system.actorOf(Worker.props(self))
      context watch routee
      ActorRefRoutee(routee)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def preStart = {
    broadcastHasWork

    //TODO Append the remaining in processing Queue to Job Queue
  }

  override def receive = {

    case NeedWork(actor) ⇒
      log.info("Worker Needs Work")
      trySendNextJob(actor)

    case Remove(job) ⇒
      log.info("Worker processed job")
      processed(job, sender())

    case CheckHasJob ⇒
      broadcastHasWork()

    case JobFailure(job, error) ⇒
      /*
      blockingClient.brpoplpush(processingQueueName, errorQueueName) // TODO Sorted Set for errorQueueName not List
      sender() ! Removed(job)
      */
  }


  def trySendNextJob(actorRef: ActorRef): Future[Unit] = {
    blockingClient.brpoplpush(redisConf.jobQueueName, redisConf.processingQueueName, timeout.duration).map {

      case Some(job) ⇒
        actorRef ! Job(job.utf8String)

      case None ⇒
        log.info("No more job")
        self ! CheckHasJob
    }
  }

  def broadcastHasWork() = {
    redis.llen(redisConf.jobQueueName).map {
      case count if count > 0 ⇒
        log.info("Found Work")
        router.route(Broadcast(HasWork), this.self)
      case _ ⇒
        log.info("No more job")
        context.system.scheduler.scheduleOnce(1 seconds, self, CheckHasJob)
    }
  }

  def processed(job: String, actorRef: ActorRef) = {
    redis.lrem(redisConf.processingQueueName, 1, job).map {
      case deletedCount ⇒
        log.info(s"Jobs deleted: ${deletedCount.toString}")
        actorRef ! Removed(job)
    }
  }
}
