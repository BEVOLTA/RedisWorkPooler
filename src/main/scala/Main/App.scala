package Main

import Sample.stream.PubSub.RedisManager
import akka.actor.{ActorRef, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import redis.RedisClient



/**
  * Created by earvinkayonga on 13/06/2016.
  */
object App {

  def mmain(args: Array[String]): Unit = {

    implicit val akkaSystem = akka.actor.ActorSystem("redis-pub-sub")
    implicit val materializer = ActorMaterializer()

    val redis: RedisClient = RedisClient(port = 32769, host = "192.168.99.100")

    val worker = akkaSystem.actorOf(Worker.props(redis, "worker"))

    val source = Source.actorPublisher[String](QueuePublisher.props(redis, "pub1", "job"))
    val sink = Sink.actorSubscriber[String](QueueSubscriber.props(redis, worker, "processing"))
    val stream = source.runWith(sink)
/*
    val redisSource = Source.actorPublisher[RedisManager.Job](RedisManager.props)
    val ref = Flow[RedisManager.Job]
      .map(_.payload.toUpperCase)
      .map { elem => println(elem); elem }
      .to(Sink.ignore)
      .runWith(redisSource)

*/
  }


}