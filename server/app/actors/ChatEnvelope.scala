package actors


import akka.actor.{Actor, ActorRef, Props}
import scala.collection.mutable.Queue

/**
 * This actor will store the request from user and send it to the manager
 */
class ChatEnvelope extends Actor {
  //the work queue and the reference of manager
  private val workQueue = Queue.empty[(ActorRef, String)]
  private var manager = List.empty[ActorRef]
  private var available = false

  import ChatEnvelope._
  def receive = {
    //connect with the worker manager
    case Connect =>
      manager ::= sender()
      available = true
    //receive new user come to chatroom
    case ReceiveNewChatter(chatter, name) =>
      manager.head ! ChatManager.NewChatter(chatter,name)
      sender() ! ChatActor.Connect
    //receive new message request
    case Message(msg) =>
      //if too many,refuse
      if (this.workQueue.size > 1000) {
        sender() ! ChatActor.Many
      }
      else {//add it to workqueue
        this.workQueue.enqueue((sender(), msg))
        manager.head ! ChatManager.Work(sender(), msg)
      }
    //receive a user close it actor
    case receiveClose1(username)=>
        manager.head ! ChatManager.receiveClose(username)

    //when a work finish, dequeue the work queue
    case Finish =>
      this.workQueue.dequeue()


    //for unhandle case
    case m =>
      println(m)
      println("Envelope Unhandled")
  }

}

object ChatEnvelope{


  case object Finish
  case class ReceiveNewChatter(chatter: ActorRef, name: String)
  case class receiveClose1(name:String)
  case class Message(msg: String)
  case object Connect


}
