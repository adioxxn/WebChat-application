package actors


import akka.actor.ActorRef
import akka.actor.Actor


class ChatWorker extends Actor {

  import ChatWorker._
  def receive = {

    case Message(msg, chatters, the_sender) =>
      println("worker receive")
      if (msg.split(":").last.equals(" Close")) {
        the_sender ! ChatActor.SendMessage("Close")
        sender() ! ChatManager.Close(the_sender)
        sender() ! ChatManager.Done(the_sender, msg)
      }
      else {
        var exist = false
        val name = msg.split("To:").last
        println(msg)
        println(name)
        //      val send =msg.substring(0,(msg.length-(name.length+3)))
        //      println("message: "+send)
        if (name.equals("All")) {
          for (c <- chatters) c._1 ! ChatActor.SendMessage(msg)
          sender() ! ChatManager.Done(the_sender, msg)
        }
        else {
          for (a <- chatters) {
            println(a._2)
            if (a._2.equals(name)) {
              exist = true
              a._1 ! ChatActor.SendMessage(msg)
            }
          }

          if (exist == false) {
            val target = msg.split(":")(0)
            println(target)
            for (c <- chatters) {
              if (c._2.equals(target)) {
                c._1 ! ChatActor.SendMessage("No such a personTo:" + name)
              }

            }
          }
          else {
            the_sender ! ChatActor.SendMessage(msg)
          }
          sender() ! ChatManager.Done(the_sender, msg)

        }

      }


    case m => println("Unhandled message")
  }
}

object ChatWorker {

  case class Message(msg: String, chatters: List[(ActorRef, String)], the_Sender: ActorRef)

}