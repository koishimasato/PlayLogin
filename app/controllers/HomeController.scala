package controllers

import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import javax.inject._
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(
                                cc: ControllerComponents,
                                silhouette: Silhouette[DefaultEnv]
) extends AbstractController (cc) {
  def index = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.index()))
  }

  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    val result = Redirect(routes.HomeController.index())
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }
}
