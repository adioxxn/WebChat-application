package models

import collection.mutable
object LoginMemoryModel {

  private val users = mutable.Map[String, String]("Jim" -> "123", "Tom" -> "123")

  def validateUser(username: String, password: String): Boolean ={
    users.get(username).map( _ == password).getOrElse(false)
  }

  def createUser(username: String, password: String): Boolean = {
    if (users.contains(username)) false else{
      users(username) = password
      true
    }
  }

}
