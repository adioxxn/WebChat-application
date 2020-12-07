package actors


import akka.actor.{Actor, ActorRef, Props}
import scala.collection.mutable.Queue
import scala.collection.mutable

class ChatManager extends Actor {

  private var chatters = List.empty[(ActorRef, String)]
  private var freeWorkers = List.empty[ActorRef]
  private var busyWorkers = List.empty[ActorRef]
  private var workqueue = Queue.empty[(ActorRef, String)]

  for (i <- 0 to 4) {
    freeWorkers ::= context.actorOf(Props(new ChatWorker()))
  }

  import ChatManager._

  //the manager that receive all the envelop
  def receive = {
    //all the user will regiester to here
    case NewChatter(chatter, name) => chatters ::= (chatter, name)
      println("Got message " + name)
    //worker done
    case Done =>
      //if still have work, work
      if (this.workqueue.size > 0) {
        val work = workqueue.dequeue
        sender() ! ChatWorker.Message(work._2, chatters, work._1)
      }
      //rest
      else {
        freeWorkers ::= sender()
        busyWorkers = busyWorkers.filter(_ != sender())
      }
    // when receive a message that one user is logout
    case Close(user) =>
      chatters = chatters.filter(_._1 != user)
    //receive message
    case Message(msg) =>
      if (this.workqueue.size > 1000) {
        sender() ! ChatActor.Many
      }
      else {

        if (freeWorkers.size > 0) {
          //          this.workqueue += ((sender(),"testing"))
          busyWorkers ::= freeWorkers.head
          freeWorkers = freeWorkers.drop(1)
          busyWorkers.last ! ChatWorker.Message(msg, chatters, sender())
        }
        else {
          workqueue.enqueue((sender(), msg))
        }
      }

    //for unhandle case
    case m => println("Unhandled")
  }

}

object ChatManager {

  case class NewChatter(chatter: ActorRef, name: String)

  case class Message(msg: String)

  case class Login(name: String, password: String)

  case class Close(user: ActorRef)

  case object Done

}
