package Main



import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akka.stream.actor.MaxInFlightRequestStrategy
import redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by earvinkayonga on 14/06/2016.
  */

object Worker {


  def props(redis: RedisClient, id: String) = {
    Props(classOf[Worker], redis, id)
  }

}

class Worker(redis: RedisClient, id: String) extends Actor with ActorLogging{

  override def receive = {
    case Some(queueName) ⇒
      DoJob(queueName)
    case _ ⇒
  }


  def DoJob(queueName: Any) : Unit = {
    Thread.sleep(2000L)
    val count: Boolean = redis.llen(queueName.toString).map {
      count ⇒ count > 0
    }.isCompleted


    val job = redis.lpop(queueName.toString).map {
      job ⇒ job.map{
        bs ⇒ bs.utf8String
      }
    }

    log.info("Worker finished " + job)

    if (count){
      DoJob(queueName)
    }

  }

}
