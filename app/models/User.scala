package models

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

case class User(
                 userID: UUID,
                 email: Option[String],
                 loginInfo: LoginInfo,
                 activated: Boolean) extends Identity {
}

