package actors


import akka.actor.{Actor, ActorRef, Props}
import scala.collection.mutable.Queue
import scala.collection.mutable

class ChatManager extends Actor{

  private var chatters = List.empty[ActorRef]
  private var freeWorkers = List.empty[ActorRef]
  private var busyWorkers = List.empty[ActorRef]
  private var workqueue = Queue.empty[(ActorRef,String)]

  for (i <-0 to 4){
    freeWorkers ::= context.actorOf(Props(new ChatWorker()))
  }


  import ChatManager._
  def receive = {
    case NewChatter(chatter) => chatters ::= chatter
      println("Got message ")
    case Done =>
      println("get done")
      println(workqueue.size)
      if(this.workqueue.size>0){
        val work = workqueue.dequeue

        sender() ! ChatWorker.Message(work._2,chatters)
      }
      else{
        freeWorkers ::= sender()
        val a = busyWorkers.indexOf(sender())
        println("busyworks: "+a)
        busyWorkers.drop(a)
      }

    case Message(msg) =>
      if (this.workqueue.size>1000){
        sender() ! ChatActor.Many
      }
      else{


        if (freeWorkers.size>0){
//          this.workqueue += ((sender(),"testing"))
          busyWorkers ::= freeWorkers.head
          freeWorkers=freeWorkers.drop(1)
          busyWorkers.head ! ChatWorker.Message(msg,chatters)
        }
        else{
          workqueue.enqueue((sender(),msg))
        }
      }


    case m => println("Unhandled")
  }

}

object ChatManager{
  case class NewChatter(chatter: ActorRef)
  case class Message(msg: String)
  case object Done
}
