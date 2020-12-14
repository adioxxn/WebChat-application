package models

import collection.mutable
object LoginMemoryModel {
  //a memory store the user info
  private val users = mutable.Map[String, String]("Jim" -> "123", "Tom" -> "123")
  /**
   * function that check whether is a valid user
   * @param username user name
   * @param password passeword
   * @return boolean
   */
  def validateUser(username: String, password: String): Boolean ={
    users.get(username).map( _ == password).getOrElse(false)
  }
  /**
   * function to create an user
   * @param username String
   * @param password String
   * @return true if not exist
   */
  def createUser(username: String, password: String): Boolean = {
    if (users.contains(username)) false else{
      users(username) = password
      true
    }
  }

}
