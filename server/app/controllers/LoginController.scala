package controllers

import javax.inject._
import models.LoginMemoryModel
import play.api.mvc.{AbstractController, ControllerComponents, MessagesAbstractController, MessagesControllerComponents}
import play.api.data._
import play.api.data.Forms._

case class LoginData(username: String, password: String)

@Singleton
class LoginController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  //the format for login/create user password or name
  val loginForm = Form(mapping(
    "Username" -> text(3, 10),
    "Password" -> text(8)
  )(LoginData.apply)(LoginData.unapply))

  //login page
  def login = Action { implicit request =>
    Ok(views.html.login1(loginForm))
  }


  //login function
  def validateLoginPost = Action { implicit request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map { args =>
      val username = args("Username").head
      val password = args("password").head
      if (LoginMemoryModel.validateUser(username, password)) {
        Redirect(routes.WebSocketChat.index()).withSession("username" -> username)
      } else {
        Redirect(routes.LoginController.login()).flashing("error" -> "invalid username/password combination")
      }
    }.getOrElse(Redirect(routes.LoginController.login()))
  }


  //create user function
  def createUser = Action { implicit request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map { args =>
      val username = args("Username").head
      val password = args("password").head
      if (LoginMemoryModel.createUser(username, password)) {
        Redirect(routes.WebSocketChat.index()).withSession("username" -> username)
      } else {
        Redirect(routes.LoginController.login())
      }
    }.getOrElse(Redirect(routes.LoginController.login())).flashing("error" -> "User creation failed")

  }


  def logout = Action {
    Redirect(routes.LoginController.login()).withNewSession
  }


}
