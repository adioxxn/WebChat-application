package actors


import akka.actor.{Actor, ActorRef, Props}
import scala.collection.mutable.Queue
import scala.collection.mutable

class ChatManager(envelope:ActorRef) extends Actor {

  private var chatters = List.empty[(ActorRef, String)]
  private var freeWorkers = List.empty[ActorRef]
  private var busyWorkers = List.empty[ActorRef]//worker, sender, msg
  private var workqueue = List.empty[(ActorRef, String)]
//  private var cEnvelope = List.empty[ActorRef]
  for (i <- 0 to 4) {
    freeWorkers ::= context.actorOf(Props(new ChatWorker()))
  }
  envelope ! ChatEnvelope.Connect

  import ChatManager._
  //the manager that receive all the envelop
  def receive = {
    //all the user will regiester to here

//    case CEnvelope =>
//      if(cEnvelope.size>0){
//        sender() ! "Who are you"
//      }
//      else{
//        cEnvelope ::= sender()
//        sender() ! ChatEnvelope.Connect
//      }


    case NewChatter(chatter, name) => chatters ::= (chatter, name)
//      println("Got message " + name)
    //worker done
    case Done(from, msg) =>
      //if still have work, work
      workqueue = workqueue.filter(x=> x._1 != from && x._2!= msg)
      if (this.workqueue.size > 0) {
        val work = workqueue.head
        sender() ! ChatWorker.Message(work._2, chatters, work._1)
      }
      //rest
      else {
        freeWorkers ::= sender()
        busyWorkers = busyWorkers.filter(_ != sender())
      }
      envelope ! ChatEnvelope.Finish

    // when receive a message that one user is logout
    case Close(user) =>
      chatters = chatters.filter(_._1 != user)
    //receive message
    case Work(from, msg) =>
      if (freeWorkers.size > 0) {
        //          this.workqueue += ((sender(),"testing"))
        busyWorkers ::= freeWorkers.head
        freeWorkers = freeWorkers.drop(1)
        busyWorkers.last ! ChatWorker.Message(msg, chatters, from)
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

  case class Close(user: ActorRef)

  case class Done(from: ActorRef, msg: String)
  case object CEnvelope

}
