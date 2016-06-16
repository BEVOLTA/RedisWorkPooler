package Main

import akka.actor.{ActorLogging, Props}
import akka.stream.actor.ActorPublisher
import redis.RedisClient

import scala.concurrent.Future
import scala.concurrent.duration._



object QueuePublisher {

  def props(redis: RedisClient, id: String, queueName: String) = {
    Props(classOf[QueuePublisher], redis, id, queueName)
  }

  case object CheckQueue
}

/**
  * Created by earvinkayonga on 13/06/2016.
  */
class QueuePublisher(
                      val redis: RedisClient,
                      val id: String,
                      val queueName: String) extends ActorPublisher[String] with ActorLogging {

  import akka.stream.actor.ActorPublisherMessage._
  import QueuePublisher._

  import context.dispatcher

  val QUEUE_NAME_PREFIX = "queue"

  val jobQueueName = s"$QUEUE_NAME_PREFIX:$queueName"

  context.system.scheduler.schedule(1 second, 2 seconds, self, CheckQueue)

  override def receive = {
    case Request(count) ⇒
      sendNext(count)
    case CheckQueue ⇒
      sendNext(totalDemand)
    case Cancel ⇒
      log.info("Cancelling stream")
      context.stop(self)

  }

  def nextJob(): Future[Option[String]] = {
    redis.rpop(jobQueueName).map(opt ⇒ opt.map(bs ⇒ bs.utf8String))
  }

  def sendNext(count: Long): Any = {
    if(isActive && totalDemand > 0) {
      nextJob().map {
        case Some(job) ⇒
          log.info("Found a job, sending to subscriber")
          onNext(job)
          sendNext(totalDemand)
        case None ⇒
          log.info("No more job in the queue")
      }
    }
  }
}
