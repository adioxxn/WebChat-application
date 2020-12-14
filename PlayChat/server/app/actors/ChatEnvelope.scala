package actors


import akka.actor.{Actor, ActorRef, Props}
import scala.collection.mutable.Queue
import scala.collection.mutable


class ChatEnvelope extends Actor {

//  private val managers = context.actorOf()
  private val workQueue = Queue.empty[(ActorRef, String)]
  private var manager = List.empty[ActorRef]
  private var available = false

  import ChatEnvelope._
  def receive = {

    case Connect =>
      println("Connect")
      manager ::= sender()
      println("manager size:",manager.size)
      available = true

    case ReceiveNewChatter(chatter, name) =>
      manager.head ! ChatManager.NewChatter(chatter,name)

    case Message(msg) =>
      println(msg)
      if (this.workQueue.size > 1000) {
        sender() ! ChatActor.Many
      }
      else {
        this.workQueue.enqueue((sender(), msg))
        println(workQueue.size)
        manager.head ! ChatManager.Work(sender(), msg)
      }

    case receiveClose1(username)=>
        manager.head ! ChatManager.receiveClose(username)


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
//          workqueue.enqueue((sender(), msg))
//        }
//      }




    case Finish =>
//      println("one job finish"+this.workQueue.size)
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