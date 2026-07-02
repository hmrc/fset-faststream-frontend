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

import forms.ActivateAccountForm
import config.{FrontendAppConfig, SecurityEnvironment}
import connectors.{TokenEmailPairInvalidException, UserManagementClient}
import connectors.UserManagementClient.TokenExpiredException
import helpers.NotificationType.*
import helpers.NotificationTypeHelper
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import security.Roles.*
import security.{SignInService, SilhouetteComponent}
import views.html.registration.Activation2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ActivationController @Inject() (
                                      config: FrontendAppConfig,
                                      mcc: MessagesControllerComponents,
                                      activationTemplate: Activation2,
                                      val secEnv: SecurityEnvironment,
                                      val silhouetteComponent: SilhouetteComponent,
                                      userManagementClient: UserManagementClient,
                                      val notificationTypeHelper: NotificationTypeHelper,
                                      signInService: SignInService,
                                      formWrapper: ActivateAccountForm)(implicit val ec: ExecutionContext) extends BaseController(config, mcc) {

  import notificationTypeHelper.*

  val marketingTrackingEnabled: Boolean = config.marketingTrackingEnabled

  def present: Action[AnyContent] = CSRSecureAction(NoRole) { implicit request =>
    implicit user => if (user.user.isActive) {
      Future.successful(Redirect(routes.HomeController.present()).flashing(warning("activation.already")))
    } else {
//      Future.successful(Ok(views.html.registration.activation(user.user.email, formWrapper.form,
//        marketingTrackingEnabled = marketingTrackingEnabled)))
      Future.successful(Ok(activationView(user.user.email, formWrapper.form)))
    }
  }

  private def activationView(
                              email: String, form: Form[ActivateAccountForm.Data],
                              notification: Option[(helpers.NotificationType, String)] = None)(
    implicit request: Request[_], user: Option[models.CachedData]): Html =
    if (config.enablePlayHmrcActivationView) {
      activationTemplate(email, form, notification, marketingTrackingEnabled)
    } else {
      views.html.registration.activation(email, form, notification, marketingTrackingEnabled)
    }

  def submit: Action[AnyContent] = CSRSecureAction(ActivationRole) { implicit request =>
    implicit user =>
      formWrapper.form.bindFromRequest().fold(
        invalidForm =>
//          Future.successful(Ok(views.html.registration.activation(user.user.email, invalidForm))),
          Future.successful(Ok(activationView(user.user.email, invalidForm))),
        data => {
          userManagementClient.activate(user.user.email, data.activationCode).flatMap { _ =>
            signInService.signInUser(user.user.copy(isActive = true))
          }.recover {
            case _: TokenExpiredException =>
//              Ok(views.html.registration.activation(
//                user.user.email,
//                formWrapper.form,
//                notification = Some(danger("expired.activation-code"))
//              ))
              Ok(activationView(user.user.email, formWrapper.form, notification = Some(danger("expired.activation-code"))))
            case _: TokenEmailPairInvalidException =>
//              Ok(views.html.registration.activation(
//                user.user.email,
//                formWrapper.form,
//                notification = Some(danger("wrong.activation-code"))
//              ))
              Ok(activationView(user.user.email, formWrapper.form, notification = Some(danger("wrong.activation-code"))))
          }
        }
      )
  }

  def resendCode: Action[AnyContent] = CSRSecureAction(ActivationRole) { implicit request =>
    implicit user =>
      userManagementClient.resendActivationCode(user.user.email).map { _ =>
        Redirect(routes.ActivationController.present).flashing(success("activation.code-resent"))
      }
  }
}
