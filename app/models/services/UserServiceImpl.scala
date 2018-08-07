package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.User

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl @Inject() (implicit ex: ExecutionContext) extends UserService {
  val users: mutable.HashMap[UUID, User] = mutable.HashMap()

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val user = users.find { case (_, user) => user.loginInfo == loginInfo }.map(_._2)
    println(user)
    Future.successful(user)
  }

  def retrieve(userID: UUID): Future[Option[User]] = {
    val user = users.get(userID)
    println(user)
    Future.successful(user)
  }

  def save(user: User): Future[User] = {
    users += (user.userID -> user)
    println(users.size)
    Future.successful(user)
  }
}

