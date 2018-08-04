package controllers

import com.mohiva.play.silhouette.api.{Env, Silhouette}
import javax.inject.Inject
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

class SignUpController @Inject()(
                                  components: ControllerComponents,
                                  silhouette: Silhouette[DefaultEnv]
                                )
                                (
                                  implicit ex: ExecutionContext
                                )
  extends AbstractController(components) {
  //  sign up page
  def view = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok("signup"))
  }
}

