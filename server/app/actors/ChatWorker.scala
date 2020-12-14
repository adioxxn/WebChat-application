package actors

import akka.pattern.CircuitBreaker
import akka.pattern.pipe
import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.ReceiveTimeout
import akka.actor.ActorLogging
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }
import scala.concurrent.duration._
import scala.collection.mutable.Queue

/**
 * The worker actor will process the work and send it to the user
 */
class ChatWorker extends Actor with ActorLogging {

  //keep all the chatters ref
  private var chatters = Map.empty[ String, ActorRef]
//  private var targetSender = Queue.empty[ActorRef]

  import ChatWorker._
  def receive = {
    //when a new user come, update the chatters info and update it to all the users
    case update(chatter, num) =>
      chatters = chatter
      if (num.equals(1)){
        chatters foreach (x => x._2 ! ChatActor.ReceiveUsers(chatters.keySet))
      }

    //when a work receive, send it to the users
    case Message(msg) =>
      val target = msg.split("To:").last //
      val the_sender = msg.split(":")(0) //
      val resend = chatters(the_sender)
      //if it is close message, mean that user log out
      if (msg.split(":").last.equals(" Close")) {
        resend ! ChatActor.End
        sender() ! ChatManager.Close(the_sender,msg)//tell others that one user logout
      }
        else if (target.equals("All")) {//if it send to the public chatroom
        chatters foreach (x => x._2 ! ChatActor.SendMessage(msg))
        sender() ! ChatManager.Done(msg)
      }
      else{
        if(chatters.contains(target)){//if target does exist or online, send it
          chatters(target) ! ChatActor.SendMessage(msg)
          resend ! ChatActor.SendMessage(msg)
          sender() ! ChatManager.Done(msg)
        }
        else{//else, send an error message
          resend ! ChatActor.SendMessage("No such a person/user currently offlineTo:" + target)
          sender() ! ChatManager.Done(msg)
        }
      }

        case m => println("Unhandled message"+m)
      }
  }


object ChatWorker {

  case class Message(msg: String)

  case class update(chatter: Map[String, ActorRef],num: Int)
}