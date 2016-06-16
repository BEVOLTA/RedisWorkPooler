package Main

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akka.stream.actor.{ActorSubscriber, MaxInFlightRequestStrategy, OneByOneRequestStrategy}
import redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global

object QueueSubscriber {
  case class Msg(id: Int, replyTo: ActorRef)
  case class Work(id: Int)
  case class Reply(id: Int)
  case class Done(id: Int)

  def props(redis: RedisClient, worker: ActorRef, queueName: String) = {
    Props(classOf[QueueSubscriber], redis, worker, queueName)
  }
}

/**
  * Created by earvinkayonga on 13/06/2016.
  */
class QueueSubscriber(val redis: RedisClient, val worker: ActorRef, val queueName: String) extends ActorSubscriber with ActorLogging {

  import akka.stream.actor.ActorSubscriberMessage._

  val processingQueueName = s"processing:$queueName"

  override def requestStrategy = OneByOneRequestStrategy

  override def receive = {

    case OnNext(job) ⇒
      log.info("Processed job {}", job.toString)
      appendJobToProcessingQueue(job.toString)

    case OnComplete ⇒
      context.stop(self)

    case OnError(cause) ⇒
      log.error(cause, "An error occured in the streal")
  }

  def appendJobToProcessingQueue(job: String) = {
    redis.lpush(processingQueueName, job).onSuccess {
      case count ⇒
        worker ! Some(processingQueueName)
    }
  }

}
