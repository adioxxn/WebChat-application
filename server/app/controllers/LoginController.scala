package controllers

import javax.inject._
import models.LoginMemoryModel
import play.api.mvc.{AbstractController, ControllerComponents, MessagesAbstractController, MessagesControllerComponents}
import play.api.data._
import play.api.data.Forms._

case class LoginData(username: String, password: String)

@Singleton
class LoginController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc){

  val loginForm = Form(mapping(
    "Username" -> text(3,10),
    "Password" -> text(8)
  )(LoginData.apply)(LoginData.unapply))

  def login = Action{ implicit request =>
    Ok(views.html.login1(loginForm))
  }
//  def validateLoginGet(Username:String, password:String) = Action{
//    Ok(s"$Username logged in with $password.")
//  }


  def validateLoginPost= Action{implicit request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map{args =>
      val username = args("Username").head
      val password = args("password").head
      if (LoginMemoryModel.validateUser(username, password)){
        Redirect(routes.WebSocketChat.index()).withSession("username" -> username)
      }else{
        Redirect(routes.LoginController.login()).flashing("error"-> "invalid username/password combination")
      }
    }.getOrElse(Redirect(routes.LoginController.login()))
  }



  def createUser =Action{implicit request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map{args =>
      val username = args("Username").head
      val password = args("password").head
      if (LoginMemoryModel.createUser(username, password)){
        Redirect(routes.WebSocketChat.index()).withSession("username" -> username)
//        Redirect(routes.WebSocketChat.index()).withSession("username" -> username)
      }else{
        Redirect(routes.LoginController.login())
      }
    }.getOrElse(Redirect(routes.LoginController.login())).flashing("error"-> "User creation failed")

  }

//  def taskList = Action{ implicit request =>
//    val usernameOption = request.session.get("username")
//    usernameOption.map{username =>
//      val tasks = LoginMemoryModel.getTasks(username)
//      Ok(views.html.taskList1(tasks))
//
//    }.getOrElse(Redirect(routes.TaskList1.login()))
////    val tasks = TaskListInMemoryModel.getTasks(username)
//
//  }

  def logout = Action{
    Redirect(routes.LoginController.login()).withNewSession
  }



}
