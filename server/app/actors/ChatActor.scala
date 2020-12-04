package actors

import akka.actor.{Actor, ActorRef, Props}

class ChatActor(out: ActorRef, manager:ActorRef) extends Actor{

  manager ! ChatManager.NewChatter(self)
  var username=""
  var password=""


  import ChatActor._
  def receive ={
    case s: String =>
      if(s.split(",").size==3 && s.split(",")(0)=="Login"){
        val temp= s.split(",")
        username = temp(1)
        password  = temp(2)
        manager ! ChatManager.Login(username,password)
      }
      else{
        manager ! ChatManager.Message(s)
      }

    case SendMessage(msg) =>
      out ! msg
    case LoginDone(msg) =>
      println("login success")


    case Many => println("Fail to send to server")
    case m => println("Unhandled message in ChatActor: "+ m )


  }

}

object ChatActor {
  def props(out: ActorRef, manager: ActorRef) = Props(new ChatActor(out,manager))

  case class SendMessage(msg: String)
  case object Many
  case class LoginDone(msg:String)
}