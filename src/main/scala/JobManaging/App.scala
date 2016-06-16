package JobManaging

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

/**
  * Created by earvinkayonga on 14/06/2016.
  */
object App {
  def mmain(args: Array[String]) {
    implicit val akkaSystem = akka.actor.ActorSystem("redis-pub-sub")
    implicit val materializer = ActorMaterializer()

    val jobManagerSource = Source.actorPublisher[JobManager.Job](JobManager.props)
    val ref = Flow[JobManager.Job]
      .map(_.payload.toUpperCase)
      .map { elem => println(elem); elem }
      .to(Sink.ignore)
      .runWith(jobManagerSource)

    ref ! JobManager.Job("a")
    ref ! JobManager.Job("b")
    ref ! JobManager.Job("c")
  }
}
