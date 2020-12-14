package controllers


import javax.inject._
import play.api.mvc._
import akka.actor.{Actor, ActorSystem, Props}
import play.api.libs.streams.ActorFlow
import akka.stream.Materializer
import actors.{ChatActor, ChatEnvelope, ChatManager}
import models.LoginMemoryModel


@Singleton
class WebSocketChat @Inject()(cc: ControllerComponents) (implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc){


  val Envelope = system.actorOf(Props[ChatEnvelope],"Envelop")
  val manager = system.actorOf(Props(classOf[ChatManager],Envelope),"Manager")

  var name=""

  def index = Action{implicit  request =>
    val usernameOption = request.session.get("username")
    usernameOption.map{username =>
      name = username
      Ok(views.html.chatPage(name))

    }.getOrElse(Redirect(routes.LoginController.login()))
  }
  def socket = WebSocket.accept[String,String]{ request =>
    ActorFlow.actorRef{ out =>
      ChatActor.props(out,Envelope,name)
    }
  }

}