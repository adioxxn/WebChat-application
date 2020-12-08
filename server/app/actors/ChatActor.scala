package actors

import akka.actor.{Actor, ActorRef, Props}

class ChatActor(out: ActorRef, manager:ActorRef,name:String) extends Actor{

  manager ! ChatEnvelope.ReceiveNewChatter(self,name)
  var username=name
  var password=""


  import ChatActor._
  def receive ={
    case s: String =>
        manager ! ChatEnvelope.Message(username + ": " +s)

    case SendMessage(msg) =>
      if(msg.equals("Close")){
        context.system.terminate()
      }
      else{
        out ! msg
      }

    case Many => println("Fail to send to server")
    case m => println("Unhandled message in ChatActor: "+ m )
  }

}

object ChatActor {
  def props(out: ActorRef, manager: ActorRef,name:String) = Props(new ChatActor(out,manager,name:String))

  case class SendMessage(msg: String)
  case object Many
  case class LoginDone(msg:String)
}