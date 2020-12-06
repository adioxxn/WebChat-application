package actors


import akka.actor.ActorRef
import akka.actor.Actor


class ChatWorker extends Actor{
  import ChatWorker._


  def receive = {

    case Message(msg, chatters, the_sender) =>
      println("worker receive")
      var exist = false
      val name = msg.split("To:").last
      println(msg)
      println(name)
      val send =msg.substring(0,(msg.length-(name.length+3)))
      println("message: "+send)
      if(name.equals("All")){
        for (c <- chatters) c._1 ! ChatActor.SendMessage(send)
        sender() ! ChatManager.Done
      }
      else{
        println(chatters.size)
        for (a <- chatters) {
          println(a._2)
          if (a._2.equals(name)){
            exist = true
            a._1 ! ChatActor.SendMessage(send)
          }
        }

        if(exist == false){
          val target = msg.split(":")(0)
          println(target)
          for (c <- chatters){
            if(c._2.equals(target)){
              c._1 ! ChatActor.SendMessage("No such person")
            }

          }
        }
        else{
          the_sender ! ChatActor.SendMessage(send)
        }
//        for (c <- chatters) c._1 ! ChatActor.SendMessage(send)
//        sender() ! ChatManager.Done
      }






    case m => println("Unhandled")
  }
}
object ChatWorker{
  case class Message(msg: String, chatters: List[(ActorRef,String)], the_Sender: ActorRef)
}