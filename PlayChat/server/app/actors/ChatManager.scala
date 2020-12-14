package actors


import akka.actor.TypedActor.dispatcher
import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{CircuitBreaker, ask, pipe}
import akka.util.Timeout

import scala.collection.mutable.Queue
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future, TimeoutException}

/**
 * Chat manager will organize all the chatter and msg work and send it to the work to process it
 * @param envelope the envelope actor will send all the work to manager
 */
class ChatManager(envelope:ActorRef) extends Actor {
  //connect with envelope
  envelope ! ChatEnvelope.Connect

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  private var chatters = Map.empty[String, ActorRef]
  private var freeWorkers = List.empty[ActorRef]
  private var allWorkers = List.empty[ActorRef]
  private var busyWorkers = Map.empty[ActorRef, String]//worker, msg
  private var workqueue = List.empty[(ActorRef, String)]
  //create default workers
  for (i <- 0 to 4) {
    val temp = context.actorOf(Props(new ChatWorker()))
    freeWorkers ::= temp
    allWorkers ::= temp
  }

  import akka.pattern.pipe
  import ChatManager._
  //the manager that receive all the envelop
  def receive = {
    //all the user will regiester to here
    case NewChatter(chatter, name) =>
      chatters = chatters+ ( name-> chatter)
      var count =0
      for (c <- allWorkers) {
        count=count+1
        c ! ChatWorker.update(chatters,count)
      }


    //worker done
    case Done(msg) =>
      workqueue = workqueue.filter(x=> x._2!= msg)
      envelope ! ChatEnvelope.Finish
      //if still have work, work
      if (this.workqueue.size > 0) {
        val work = workqueue.head
        sender() ! ChatWorker.Message(work._2)
        busyWorkers.-(sender())
        busyWorkers = busyWorkers+(sender() -> work._2)

      }
      //rest
      else {
        freeWorkers ::= sender()
        busyWorkers.-(sender())
      }


    // when receive a message that one user is logout by closing the tag/ browser
    case Close(user, msg) =>
      chatters = chatters.-(user)
      workqueue = workqueue.filter(x=> x._1 != user && x._2!= msg)
      var count=0
      for (c <- allWorkers) {
        count = count+1
        c ! ChatWorker.update(chatters,count)
      }
      //if still have work, worker keep working
      if (this.workqueue.size > 0) {
        val work = workqueue.head
        sender() ! ChatWorker.Message(work._2)
        busyWorkers.-(sender())
        busyWorkers = busyWorkers+(sender() -> work._2)

      }
      //rest
      else {
        freeWorkers ::= sender()
        busyWorkers.-(sender())
      }
      envelope ! ChatEnvelope.Finish


    //receive message and send the work to user
    case Work(from, msg) =>
      if (freeWorkers.size > 0) {

        val worker = freeWorkers.head
        freeWorkers = freeWorkers.drop(1)
        busyWorkers = busyWorkers+(worker -> msg)
        worker ! ChatWorker.Message(msg)


      }
      else {
        workqueue ::= (from, msg)
      }
    // receive one user normal logout, update all active user
    case receiveClose(username) =>
      chatters = chatters.-(username)
      var count = 1
      for (c <- allWorkers)
        c ! ChatWorker.update(chatters,count)
        count = count+1


    //for unhandle case
    case m =>
      println(m)
      println("Unhandled")
  }

}

object ChatManager {

  case class NewChatter(chatter: ActorRef, name: String)

  case class Message(msg: String)

  case class Work(from: ActorRef, msg: String)

  case class Login(name: String, password: String)

  case class Close(user: String, msg: String)

  case class receiveClose(name: String)

  case class Done( msg: String)
  case object CEnvelope
}
