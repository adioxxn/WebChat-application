package actors


import akka.actor.ActorRef
import akka.actor.Actor


class ChatWorker extends Actor{
  import ChatWorker._


  def receive = {

    case Message(msg, chatters) =>
      for (c <- chatters) c ! ChatActor.SendMessage(msg)
      sender() ! ChatManager.Done

    case m => println("Unhandled")
  }
}
object ChatWorker{
  case class Message(msg: String, chatters: List[ActorRef])
}