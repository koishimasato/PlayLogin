package controllers

import java.util.UUID

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignUpForm
import javax.inject.Inject
import models.User
import models.services.UserService
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

class SignUpController @Inject()(
                                  components: ControllerComponents,
                                  silhouette: Silhouette[DefaultEnv],
                                  userService: UserService,
                                  authInfoRepository: AuthInfoRepository,
                                  passwordHasherRegistry: PasswordHasherRegistry,
                                )
                                (
                                  implicit ex: ExecutionContext
                                )
  extends AbstractController(components) {
  //  sign up page
  def view = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.signUp(SignUpForm.form)))
  }

  def submit = silhouette.UnsecuredAction.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(
      // エラー時
      formWithError => Future.successful(BadRequest(views.html.signUp(formWithError))),

      data => {
        val result = Redirect(routes.SignInController.view()).flashing("info" -> "sign.up.email.sent")

        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)

        userService.retrieve(loginInfo).flatMap {
          case Some(user) => Future.successful(result)
          case None =>

            val user = User(
              userID = UUID.randomUUID(),
              loginInfo = loginInfo,
              email = Some(data.email),
              activated = true
            )

            val authInfo = passwordHasherRegistry.current.hash(data.password)

            for {
              _ <- userService.save(user)
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
            } yield {
              result
            }
        }
      }
    )
  }
}

