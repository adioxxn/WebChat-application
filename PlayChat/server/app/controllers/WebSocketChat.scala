package controllers


import javax.inject._
import play.api.mvc._
import akka.actor.{Actor, ActorSystem, Props}
import play.api.libs.streams.ActorFlow
import akka.stream.Materializer
import actors.{ChatActor, ChatEnvelope, ChatManager}
import models.LoginMemoryModel

/**
 * this the the websocket class that create once the user login
 * @param cc using the controller components
 * @param system using the actor system
 * @param mat using materializer
 */
@Singleton
class WebSocketChat @Inject()(cc: ControllerComponents) (implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc){

  //create an envelop class and the worker manager
  val Envelope = system.actorOf(Props[ChatEnvelope],"Envelop")
  val manager = system.actorOf(Props(classOf[ChatManager],Envelope),"Manager")

  var name=""

  /**
   * take the input from the login page
   * @return chatroom page
   */
  def index = Action{implicit  request =>
    val usernameOption = request.session.get("username")
    usernameOption.map{username =>
      name = username
      Ok(views.html.chatPage(name))
    }.getOrElse(Redirect(routes.LoginController.login()))
  }

  /**
   * open the websocket and create actor
   * @return
   */
  def socket = WebSocket.accept[String,String]{ request =>
    ActorFlow.actorRef{ out =>
      ChatActor.props(out,Envelope,name)
    }
  }

}