package controllers

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignInForm
import javax.inject.Inject
import models.services.UserService
import play.api.Configuration
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import utils.auth.DefaultEnv
import net.ceedubs.ficus.Ficus._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class SignInController @Inject() (
                                   components: ControllerComponents,
                                   silhouette: Silhouette[DefaultEnv],
                                   userService: UserService,
                                   configuration: Configuration,
                                   credentialsProvider: CredentialsProvider,
                                   clock: Clock,
                                   authInfoRepository: AuthInfoRepository,
                                 )(
                                   implicit
                                   ex: ExecutionContext
                                 ) extends AbstractController(components) {

  def view = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signIn(SignInForm.form)))
  }

  def submit = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    val bform = SignInForm.form.bindFromRequest

    bform.fold(
      form => Future.successful(BadRequest(views.html.signIn(form))),

      data => {
        val credentials = Credentials(data.email, data.password)

        // authInfoRepositoryから認証情報にあったデータがあるか（=ユーザーが登録されているか）を確認

        val credential = credentialsProvider.authenticate(credentials)

        credential.flatMap { loginInfo =>

          val result = Redirect(routes.HomeController.index())

          // loginInfoに見合ったユーザーを見つける
          userService.retrieve(loginInfo).flatMap {
            case Some(user) if !user.activated => Future.successful(Redirect(routes.SignInController.view()))
            case Some(user) =>
              val c = configuration.underlying

              // CookieAuthenticatorを生成
              silhouette.env.authenticatorService.create(loginInfo).map {
                case authenticator if data.rememberMe =>
                  authenticator.copy(
                    expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                    idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
                    cookieMaxAge = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge")
                  )
                case authenticator => authenticator

              }.flatMap { authenticator =>
                // Cookieを生成
                silhouette.env.authenticatorService.init(authenticator).flatMap { v =>

                  // Result型にトークンを付け加えたものを返す
                  silhouette.env.authenticatorService.embed(v, result)
                }
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case _: ProviderException =>
            Redirect(routes.SignInController.view()).flashing("error" -> "invalid.credentials")
        }
      }
    )
  }
}

