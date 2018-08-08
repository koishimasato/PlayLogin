package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.User
import models.services.UserServiceImpl._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class UserServiceImpl @Inject()(implicit ex: ExecutionContext) extends UserService {
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val user = users.find { case (_, user) => user.loginInfo == loginInfo }.map(_._2)
    Future.successful(user)
  }

  def retrieve(userID: UUID): Future[Option[User]] = {
    val user = users.get(userID)
    Future.successful(user)
  }

  def save(user: User): Future[User] = {
    users += (user.userID -> user)
    Future.successful(user)
  }
}

object UserServiceImpl {
  val users: mutable.HashMap[UUID, User] = mutable.HashMap()
}
