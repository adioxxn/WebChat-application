package actors

import akka.actor.{Actor, ActorRef, Props}

class ChatActor(out: ActorRef, manager:ActorRef) extends Actor{

  manager ! ChatManager.NewChatter(self)
  import ChatActor._
  def receive ={
    case s: String => manager ! ChatManager.Message(s)
    case SendMessage(msg) => out ! msg

    case Many => println("Fail to send to server")
    case m => println("Unhandled message in ChatActor: "+ m )
  }

}

object ChatActor {
  def props(out: ActorRef, manager: ActorRef) = Props(new ChatActor(out,manager))

  case class SendMessage(msg: String)
  case object Many
}