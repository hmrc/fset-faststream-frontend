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

import forms.LockAccountForm
import config.{FrontendAppConfig, SecurityEnvironment, TrackingConsentConfig}
import helpers.NotificationTypeHelper
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, Flash, MessagesControllerComponents, Request}
import play.twirl.api.Html
import security.SilhouetteComponent
import views.html.index.LockedAccount2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LockAccountController @Inject()(config: FrontendAppConfig,
                                      mcc: MessagesControllerComponents,
                                      lockedAccountTemplate: LockedAccount2,
                                      val secEnv: SecurityEnvironment,
                                      val silhouetteComponent: SilhouetteComponent,
                                      val notificationTypeHelper: NotificationTypeHelper
                                     )(implicit val ec: ExecutionContext)
  extends BaseController(config, mcc) {

  def present: Action[AnyContent] = CSRUserAwareAction { implicit request =>
    implicit user =>
      val email = request.session.get("email")
//      Future.successful(Ok(views.html.index.locked(
//        LockAccountForm.form.fill(LockAccountForm.Data(email.getOrElse("")))
//      )))
      Future.successful(Ok(lockedAccountView(
        LockAccountForm.form.fill(LockAccountForm.Data(email.getOrElse("")))
      )))
  }

  private def lockedAccountView(form: Form[LockAccountForm.Data])(
    implicit request: Request[_], user: Option[models.CachedData], flash: Flash, messages: Messages): Html = {
    implicit val feedBackUrl: String = config.feedbackUrl
    implicit val trackingConsentConfig: TrackingConsentConfig = config.trackingConsentConfig
    if (config.enablePlayHmrcLockedAccountView) {
      lockedAccountTemplate(form)
    } else {
      views.html.index.locked(form)
    }
  }

  def submit: Action[AnyContent] = CSRUserAwareAction { implicit request =>
    implicit user =>
      LockAccountForm.form.bindFromRequest().fold(
        _ => Future.successful(Redirect(routes.LockAccountController.present)),
        data => Future.successful(Redirect(routes.PasswordResetController.presentReset)
          addingToSession ("email" -> data.email))
      )
  }
}
