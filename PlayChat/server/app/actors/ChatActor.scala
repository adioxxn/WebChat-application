package actors

import java.io.{File, FileNotFoundException, PrintWriter}

import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout}
import java.util.concurrent.TimeUnit

import scala.concurrent.{Await, ExecutionContext, Future, TimeoutException}
import scala.concurrent.duration._
import akka.actor.ReceiveTimeout
import akka.pattern.ask
import akka.util.Timeout

import scala.io.Source


/**
 * the actor control the user message
 * @param out web socket
 * @param envelop envelop actor that receive all message from user
 * @param name username
 */
class ChatActor(out: ActorRef, envelop:ActorRef,name:String) extends Actor{
  //send a message to the envelop
  implicit val timeout: Timeout = 5.seconds
  //use ask pattern to handle request with timeout
  val future = envelop ? ChatEnvelope.ReceiveNewChatter(self,name)
  try{
    Await.result(future, timeout.duration)

  } catch {
    case e: TimeoutException =>

      envelop ! ChatEnvelope.ReceiveNewChatter(self,name)

  }
  envelop ! ChatEnvelope.ReceiveNewChatter(self,name)
  //if not sending any messaage for 3 mins, close it
  context.setReceiveTimeout(180 seconds)


  var username=name

  var allMessage = List.empty[String]
  load()
  import ChatActor._
  def receive ={

    case Connect=>
      println("connect")
    //send the message
    case s: String =>
      envelop ! ChatEnvelope.Message(username + ": " +s)
    //receive message from the sserver
    case SendMessage(msg) =>
      allMessage ::= msg
      if(msg.equals("Close")){
        self ! ChatActor.End
      }
      else{
        out ! msg
      }
    //receive new activie user list
    case ReceiveUsers(users) =>
      val user = users.toArray
      val userlist ="ActiveUser,"+ user.mkString(",")
      out ! userlist

    //if user turn it off, close actor
    case End =>
      write()
      context.stop(self)

    case Many => println("Fail to send to server")
    //if receive time out, close actor
    case ReceiveTimeout =>
      envelop ! ChatEnvelope.receiveClose1(username)
      write()
      context.stop(self)

    case m => println("Unhandled message in ChatActor: "+ m )
  }
//

  /**
   * update the data
   */
  def write():Unit={
    val fileout = username+".txt"
    val writer = new PrintWriter(new File(fileout))
    for (a<- allMessage.reverse){
      val target = a.split("To:").last //All
      if(!target.equals("All") && !a.contains("No such a personTo:")){
        writer.write(a+"\n")
      }
    }
    writer.close()
  }

  /**
   * load the data for user
   */
  def load(): Unit={
    val fileout = username+".txt"
    try {
      val lines = Source.fromFile(fileout).getLines().toArray
      for (a <- lines) {
        allMessage ::=a
        out ! a
      }
    }
    catch{
      case e: FileNotFoundException =>
        println("new user create new file")
    }

  }

}

object ChatActor {
  def props(out: ActorRef, manager: ActorRef,name:String) = Props(new ChatActor(out,manager,name:String))
  case class SendMessage(msg: String)
  case class ReceiveUsers(user: Set[String])
  case object Many
  case object Connect
  case object End
  case class LoginDone(msg:String)
}