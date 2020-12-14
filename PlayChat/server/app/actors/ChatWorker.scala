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

class ChatWorker extends Actor with ActorLogging {
//  context.setReceiveTimeout(30 seconds)
  //  import context.dispatcher
  //
  //  val breaker =
  //    new CircuitBreaker(context.system.scheduler, maxFailures = 5, callTimeout = 10.seconds, resetTimeout = 1.minute)
  //      .onOpen(notifyMeOnOpen())
  //  def notifyMeOnOpen(): Unit =
  //    log.warning("My CircuitBreaker is now open, and will not close for one minute")

  private var chatters = Map.empty[ String, ActorRef]
  private var targetSender = Queue.empty[ActorRef]

  import ChatWorker._

  def receive = {

    case update(chatter, num) =>
      chatters = chatter
      if (num.equals(1)){
        chatters foreach (x => x._2 ! ChatActor.ReceiveUsers(chatters.keySet))
      }

//    case updateToAll =>
//      chatters foreach (x => x._2 ! ChatActor.ReceiveUsers(chatters.keySet))
//      sender() ! ChatManager.Done("update")

    case Message(msg) =>
      println("worker receive") // Tom: 22To:All
      val target = msg.split("To:").last //All
      val the_sender = msg.split(":")(0) //Tom
      var exist = false
      println(msg)
      val resend = chatters(the_sender)
      if (msg.split(":").last.equals(" Close")) {
        resend ! ChatActor.End
        sender() ! ChatManager.Close(the_sender,msg)
      }
        else if (target.equals("All")) {
        chatters foreach (x => x._2 ! ChatActor.SendMessage(msg))
        sender() ! ChatManager.Done(msg)
      }
      else{
        if(chatters.contains(target)){
          chatters(target) ! ChatActor.SendMessage(msg)
          resend ! ChatActor.SendMessage(msg)
          sender() ! ChatManager.Done(msg)
        }
        else{
          resend ! ChatActor.SendMessage("No such a personTo:" + target)
          sender() ! ChatManager.Done(msg)
        }
      }





        //      if (msg.split(":").last.equals(" Close")) {
        ////        the_sender ! ChatActor.SendMessage("Close")
        //        the_sender ! ChatActor.End
        ////        breaker.withCircuitBreaker(Future(dangerousCall)).pipeTo(sender())
        //        sender() ! ChatManager.Close(the_sender, msg)
        ////        sender() ! ChatManager.Done(the_sender, msg)
        ////        context.setReceiveTimeout(Duration.Undefined)
        //      }
        //      else {
        //        var exist = false
        //        val name = msg.split("To:").last
        //        println(msg)
        //        println(name)
        //        //      val send =msg.substring(0,(msg.length-(name.length+3)))
        //        //      println("message: "+send)
        //        if (name.equals("All")) {
        //          for (c <- chatters) c._1 ! ChatActor.SendMessage(msg)
        //          sender() ! ChatManager.Done(the_sender, msg)
        //        }
        //        else {
        //          for (a <- chatters) {
        //            if (a._2.equals(name)) {
        //              exist = true
        //              a._1 ! ChatActor.SendMessage(msg)
        //            }
        //          }
        //          if (exist == false) {
        //            val target = msg.split(":")(0)
        //            println(target)
        //            for (c <- chatters) {
        //              if (c._2.equals(target)) {
        //
        //                c._1 ! ChatActor.SendMessage("No such a personTo:" + name)
        //              }
        //            }
        //          }
        //          else {
        //            the_sender ! ChatActor.SendMessage(msg)
        //          }
        //          sender() ! ChatManager.Done(the_sender, msg)
        //        }
        //      }


        case m => println("Unhandled message"+m)
      }
  }


object ChatWorker {

  case class Message(msg: String)

  case class update(chatter: Map[String, ActorRef],num: Int)
  case object updateToAll
}