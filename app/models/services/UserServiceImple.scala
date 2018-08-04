package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.User

import scala.concurrent.{ExecutionContext, Future}
class UserServiceImpl @Inject() (implicit ex: ExecutionContext) extends UserService {
  val user = Option(User(email = Some("test@example.com"), activated = false))

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = Future.successful(user)
  def retrieve(id: UUID): Future[Option[User]] = Future.successful(user)
  def save(user: User): Future[User] = Future.successful(user)
}

