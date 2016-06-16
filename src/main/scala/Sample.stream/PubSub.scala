package Sample.stream

import redis.RedisClient
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{ Success, Failure }
import akka.actor.{ ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.stream.actor.{ActorPublisher}
import akka.stream.scaladsl._



object PubSub {


  object RedisManager {
    def props: Props = Props[RedisManager]

    final case class Job(payload: String)
    case object JobAccepted
    case object JobDenied
  }

  class RedisManager extends ActorPublisher[RedisManager.Job] {
    import akka.stream.actor.ActorPublisherMessage._
    import RedisManager._

    val MaxBufferSize = 100
    var buf = Vector.empty[Job]

    def receive = {
      case job: Job if buf.size == MaxBufferSize =>
        sender() ! JobDenied
      case job: Job =>
        sender() ! JobAccepted
        if (buf.isEmpty && totalDemand > 0)
          onNext(job)
        else {
          buf :+= job
          deliverBuf()
        }
      case Request(_) =>
        deliverBuf()
      case Cancel =>
        context.stop(self)
    }

    final def deliverBuf(): Unit =
      if (totalDemand > 0) {
        /*
         * totalDemand is a Long and could be larger than
         * what buf.splitAt can accept
         */
        if (totalDemand <= Int.MaxValue) {
          val (use, keep) = buf.splitAt(totalDemand.toInt)
          buf = keep
          use foreach onNext
        } else {
          val (use, keep) = buf.splitAt(Int.MaxValue)
          buf = keep
          use foreach onNext
          deliverBuf()
        }
      }
  }

}


