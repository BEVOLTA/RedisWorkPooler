package WorkPool


import akka.actor.{Actor, ActorLogging, ActorRef, Props}

/**
  * Created by earvinkayonga on 15/06/2016.
  */

object Worker {
  def props(parent: ActorRef): Props = Props(classOf[Worker], parent)

  case class JobSuccess(Job: String)
  case class JobFailure(Job: String, error: String)
}

class Worker(parent: ActorRef) extends Actor with ActorLogging {

  import WorkPool.QueueManager._

  import context.dispatcher

  //val parent = context.parent

  /**
    * Creates one actor to actually do the job
    */
  val workExecutor: ActorRef = context.watch(context.system.actorOf(WorkerExec.props(self)))

  override def preStart = {
    log.info("Need Work sent to Queue Manager")
    parent ! NeedWork(self)
  }

  override def receive = available

  def available: Receive = {

    case Job(job) ⇒
      log.info("Work received from Queue Manager and sent WorkerExec")
      workExecutor ! Job(job)
      context.become(working)

    case HasWork ⇒
      log.info("Queue Manager has work")
      parent ! NeedWork(self)
  }

  def working : Receive = {

    case Job(job) ⇒
      log.info("Sorry I'm working.")
      // TODO Append back to redis

    case Worker.JobSuccess(job) ⇒
      log.info("Work succesfully done by Worker Exec")
      parent ! Remove(job)
      context.become(waitingForProcessedAck)

    case Worker.JobFailure(job, error) ⇒
      log.info("Work failed by Worker Exec ")
      parent ! JobFailure(job, error)
      context.become(waitingForProcessedAck)
  }

  def waitingForProcessedAck: Receive = {

    case Removed(job) ⇒
      log.info("Job removed from Jobs Queue")
      context.become(available)
      parent ! NeedWork(self)
  }
}

