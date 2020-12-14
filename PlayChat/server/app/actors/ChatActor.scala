package actors

import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration._
import akka.actor.ReceiveTimeout



class ChatActor(out: ActorRef, manager:ActorRef,name:String) extends Actor{

  manager ! ChatEnvelope.ReceiveNewChatter(self,name)
  context.setReceiveTimeout(180 seconds)
  var username=name
  var password=""

//  context.setReceiveTimeout(5.seconds)
  import ChatActor._
  def receive ={
    case s: String =>
        manager ! ChatEnvelope.Message(username + ": " +s)

    case SendMessage(msg) =>
      if(msg.equals("Close")){
        context.stop(self)
      }
      else{
        out ! msg
      }
    case ReceiveUsers(users) =>
      val user = users.toArray
      val userlist ="ActiveUser,"+ user.mkString(",")
      println("all users:"+user.mkString(","))
      out ! userlist


    case End =>
      context.stop(self)

    case Many => println("Fail to send to server")

    case ReceiveTimeout =>
      manager ! ChatEnvelope.receiveClose1(username)
      context.stop(self)

    case m => println("Unhandled message in ChatActor: "+ m )
  }

}

object ChatActor {
  def props(out: ActorRef, manager: ActorRef,name:String) = Props(new ChatActor(out,manager,name:String))

  case class SendMessage(msg: String)
  case class ReceiveUsers(user: Set[String])
  case object Many
  case object End
  case class LoginDone(msg:String)
}