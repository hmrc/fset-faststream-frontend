/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import forms.SignInForm
import play.silhouette.api.util.Credentials
import config.{FrontendAppConfig, SecurityEnvironment}
import helpers.NotificationTypeHelper

import javax.inject.{Inject, Singleton}
import models.ApplicationRoute
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import security.*

import scala.concurrent.{ExecutionContext, Future}
import views.html.index.SignIn2

@Singleton
class SignInController @Inject()(
                                  config: FrontendAppConfig,
                                  mcc: MessagesControllerComponents,
                                  signInTemplate: SignIn2,
                                  val secEnv: SecurityEnvironment,
                                  val silhouetteComponent: SilhouetteComponent,
                                  val notificationTypeHelper: NotificationTypeHelper,
                                  val signInService: SignInService,
                                  signInForm: SignInForm)(implicit val ec: ExecutionContext)
  extends BaseController(config,mcc) {
  import notificationTypeHelper._

  def present: Action[AnyContent] = CSRUserAwareAction { implicit request =>
    implicit user =>
      request.identity match {
        case None =>
          Future.successful(Ok(signInView(signInForm.form)))
        case Some(u) =>
          Future.successful(Redirect(routes.HomeController.present()))
      }
  }

  private def signInView(form: Form[SignInForm.Data])(implicit request: Request[_], user: Option[models.CachedData]): Html =
    if (config.enablePlayHmrcSignInView) {
      signInTemplate(form)
    } else {
      views.html.index.signin(form)
    }

  def sdipPresent: Action[AnyContent] = CSRUserAwareAction { implicit request =>
    implicit user =>
      request.identity match {
        case None =>
          Future.successful(
            Ok(signInView(signInForm.form.fill(SignInForm.Data(signIn = "", signInPassword = "", Some(ApplicationRoute.SdipFaststream)))))
          )
        case Some(u) =>
          Future.successful(Redirect(routes.HomeController.present()))
      }
  }

  // scalastyle:off cyclomatic.complexity
  def signIn: Action[AnyContent] = CSRUserAwareAction { implicit request =>
    implicit user =>
      signInForm.form.bindFromRequest().fold(
        invalidForm =>
          Future.successful(Ok(signInView(invalidForm))),
        data => secEnv.credentialsProvider.authenticate(Credentials(data.signIn, data.signInPassword)).flatMap {
          case Right(usr) if usr.lockStatus == "LOCKED" => Future.successful(
            Redirect(routes.LockAccountController.present).addingToSession("email" -> usr.email))
          case Right(usr) if usr.isActive =>
//            if (data.route.contains(ApplicationRoute.SdipFaststream.toString)) {
//              signInService.signInUser(usr, Redirect(routes.ConsiderForSdipController.present))
//            } else {
              signInService.signInUser(usr)
//            }
          case Right(usr) => signInService.signInUser(usr, redirect = Redirect(routes.ActivationController.present))
          case Left(InvalidRole) => Future.successful(signInService.showErrorLogin(data, errorMsg = "error.invalidRole"))
          case Left(InvalidCredentials) => Future.successful(signInService.showErrorLogin(data))
          case Left(LastAttempt) => Future.successful(signInService.showErrorLogin(data, errorMsg = "last.attempt"))
          case Left(AccountLocked) => Future.successful(Redirect(routes.LockAccountController.present)
            .addingToSession("email" -> data.signIn))
        }
      )
  }
  // scalastyle:on cyclomatic.complexity

  def signOut: Action[AnyContent] = CSRUserAwareAction { implicit request =>
    implicit user =>
      signInService.logOutAndRedirectUserAware(
        successAction = Redirect(routes.SignInController.present)
          .flashing(success("feedback", config.feedbackUrl)).withNewSession,
        failAction = Redirect(routes.SignInController.present)
          .flashing(danger("You have already signed out")).withNewSession
      )
  }
}
