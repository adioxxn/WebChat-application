package actors


import akka.actor.TypedActor.dispatcher
import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{CircuitBreaker, ask, pipe}
import akka.util.Timeout

import scala.collection.mutable.Queue
import scala.collection.mutable
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future, TimeoutException}

class ChatManager(envelope:ActorRef) extends Actor {

  envelope ! ChatEnvelope.Connect
  println("connecting")


  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds
//  val breaker = new CircuitBreaker(context.system.scheduler, maxFailures = 5, callTimeout = 10.seconds,
//    resetTimeout = 1.minute).onOpen(println("circuit breaker opened!"))
  private var chatters = Map.empty[String, ActorRef]
  private var freeWorkers = List.empty[ActorRef]
  private var allWorkers = List.empty[ActorRef]
  private var busyWorkers = Map.empty[ActorRef, String]//worker, msg
  private var workqueue = List.empty[(ActorRef, String)]
//  private var cEnvelope = List.empty[ActorRef]
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

    //      allWorkers.head ! ChatWorker.updateToAll

    //worker done
    case Done(msg) =>
      //if still have work, work
      workqueue = workqueue.filter(x=> x._2!= msg)
      println("work Done")
      envelope ! ChatEnvelope.Finish

      if (this.workqueue.size > 0) {
        val work = workqueue.head
        sender() ! ChatWorker.Message(work._2)
        busyWorkers.-(sender())
        busyWorkers = busyWorkers+(sender() -> work._2)


//        val future = sender() ? ChatWorker.Message(work._2)
//        try{
//          Await.result(future, timeout.duration)
//
//        } catch {
//          case e: TimeoutException =>
//            println("catch timeout Exception")
//            allWorkers=allWorkers.filter(_ != busyWorkers.last)
//            context.stop(busyWorkers.last._1)
//            busyWorkers = busyWorkers.filter(_ != busyWorkers.last)
//            val temp =context.actorOf(Props(new ChatWorker()))
//            freeWorkers ::= temp
//            allWorkers ::= temp
//        }
      }
      //rest
      else {
        freeWorkers ::= sender()
        busyWorkers.-(sender())
      }


    // when receive a message that one user is logout
    case Close(user, msg) =>
      chatters = chatters.-(user)
      workqueue = workqueue.filter(x=> x._1 != user && x._2!= msg)
      var count=0
      for (c <- allWorkers) {
        count = count+1
        c ! ChatWorker.update(chatters,count)
      }

      if (this.workqueue.size > 0) {
        val work = workqueue.head
//        val future = sender() ? ChatWorker.Message(work._2)
//        try{
//          Await.result(future, timeout.duration)
//
//        } catch {
//          case e: TimeoutException =>
//            println("catch timeout Exception")
//            allWorkers=allWorkers.filter(_ != busyWorkers.last)
//            context.stop(busyWorkers.last._1)
//            busyWorkers = busyWorkers.filter(_ != busyWorkers.last)
//            val temp =context.actorOf(Props(new ChatWorker()))
//            freeWorkers ::= temp
//            allWorkers ::= temp
//        }
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


    //receive message
    case Work(from, msg) =>
      if (freeWorkers.size > 0) {
        //          this.workqueue += ((sender(),"testing"))
//        busyWorkers ::= (freeWorkers.head,msg)

        val worker = freeWorkers.head
        freeWorkers = freeWorkers.drop(1)
        busyWorkers = busyWorkers+(worker -> msg)
        worker ! ChatWorker.Message(msg)
//        val future: Future[String] = ask(ChatWorker, Message(msg, chatters, from)).mapTo[String]
//        val result = Await.result(future, 1 second)
//        Await.result(busyWorkers.last ? Message(msg, chatters, from), 2 seconds)

//        val askFuture = breaker.withCircuitBreaker(busyWorkers.last ? Message(msg, chatters, from),2 seconds())
//        println(askFuture)


//        val future = busyWorkers.last._1 ? ChatWorker.Message(msg)
//        try{
//          Await.result(future, timeout.duration)
//
//        } catch {
//          case e: TimeoutException =>
//            println("catch timeout Exception")
//            allWorkers=allWorkers.filter(_ != busyWorkers.last)
//            context.stop(busyWorkers.last._1)
//            busyWorkers = busyWorkers.filter(_ != busyWorkers.last)
//            val temp =context.actorOf(Props(new ChatWorker()))
//            freeWorkers ::= temp
//            allWorkers ::= temp
//        }
//        envelope ! ChatEnvelope.Finish

        //        workqueue = workqueue.filter(x=> x._2!= msg)


      }
      else {
        workqueue ::= (from, msg)
      }


//    case Message(msg) =>
//      if (this.workqueue.size > 1000) {
//        sender() ! ChatActor.Many
//      }
//      else {
//
//        if (freeWorkers.size > 0) {
//          //          this.workqueue += ((sender(),"testing"))
//          busyWorkers ::= freeWorkers.head
//          freeWorkers = freeWorkers.drop(1)
//          busyWorkers.last ! ChatWorker.Message(msg, chatters, sender())
//        }
//        else {
//          workqueue ::= (sender(), msg)
//        }
//      }
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
