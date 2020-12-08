package actors


import akka.actor.{Actor, ActorRef, Props}
import scala.collection.mutable.Queue
import scala.collection.mutable


class ChatEnvelope extends Actor {

//  private val managers = context.actorOf()
  private var workQueue = Queue.empty[(ActorRef, String)]
  private var manager = List.empty[ActorRef]
  private var available = false

  import ChatEnvelope._
  def receive = {

    case ReceiveNewChatter(chatter, name) =>
      println("sfs")
      manager.head ! ChatManager.NewChatter(chatter,name)

    case Message(msg) =>
      if (this.workQueue.size > 1000) {
        sender() ! ChatActor.Many
      }
      else {
        workQueue.enqueue((sender(), msg))
        println(workQueue.size)
        manager.head ! ChatManager.Work(sender(), msg)
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
//          workqueue.enqueue((sender(), msg))
//        }
//      }



    case Finish =>
      workQueue.dequeue()


    case Connect =>
      manager ::= sender()
      available = true


    //for unhandle case

    case m =>
      println(m)
      println("Envelope Unhandled")
  }

}

object ChatEnvelope{


  case object Finish
  case class ReceiveNewChatter(chatter: ActorRef, name: String)

  case class Message(msg: String)
  case object Connect


}
