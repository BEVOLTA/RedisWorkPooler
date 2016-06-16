package WorkPool

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

/**
  * Created by earvinkayonga on 15/06/2016.
  */


object WorkerExec {
  def props(parent: ActorRef) = Props(classOf[WorkerExec], parent)
}

class WorkerExec(parent: ActorRef) extends Actor with ActorLogging {

  override def preStart = {
    log.info("Worker Exec Created")
  }

  override def receive = {
    case WorkPool.QueueManager.Job(job) ⇒
      log.info("Worker Exec is working")
      Thread.sleep(10000L)
      parent ! WorkPool.Worker.JobSuccess(job)
  }
}