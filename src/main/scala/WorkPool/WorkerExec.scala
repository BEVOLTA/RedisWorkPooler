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
      Thread.sleep(500L)
      log.info("Worker Exec is working")
      parent ! WorkPool.Worker.JobSuccess(job)
  }
}