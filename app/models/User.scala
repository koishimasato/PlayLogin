package models

import com.mohiva.play.silhouette.api.Identity

case class User(
                 email: Option[String],
                 activated: Boolean) extends Identity {
}

