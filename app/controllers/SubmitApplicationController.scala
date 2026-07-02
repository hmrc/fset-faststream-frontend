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

import config.{FrontendAppConfig, SecurityEnvironment}
import connectors.ApplicationClient
import connectors.ApplicationClient.CannotSubmit
import helpers.NotificationTypeHelper
import models.ApplicationRoute.ApplicationRoute
import models.CachedDataWithApp
import play.api.mvc.{MessagesControllerComponents, Request}
import play.twirl.api.Html
import security.Roles.{AbleToWithdrawApplicationRole, SubmitApplicationRole}
import security.SilhouetteComponent
import views.html.application.{Submit2, Submitted2}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmitApplicationController @Inject()(
                                             config: FrontendAppConfig,
                                             mcc: MessagesControllerComponents,
                                             submitTemplate: Submit2,
                                             submittedTemplate: Submitted2,
                                             val secEnv: SecurityEnvironment,
                                             val silhouetteComponent: SilhouetteComponent,
                                             val notificationTypeHelper: NotificationTypeHelper,
                                             applicationClient: ApplicationClient
                                           )(
  implicit val ec: ExecutionContext) extends BaseController(config, mcc) with CampaignAwareController {

  val appRouteConfigMap: Map[ApplicationRoute, ApplicationRouteState] = config.applicationRoutesFrontend

  import notificationTypeHelper.*

  implicit val marketingTrackingEnabled: Boolean = config.marketingTrackingEnabled

  def presentSubmit = CSRSecureAppAction(SubmitApplicationRole) { implicit request =>
    implicit user =>
      if (canApplicationBeSubmitted(user.application.overriddenSubmissionDeadline)(user.application.applicationRoute)) {
        Future.successful(Ok(submitView()))
      } else {
        Future.successful(Redirect(routes.HomeController.present()))
      }
  }

  private def submitView()(implicit request: Request[_], user: CachedDataWithApp): Html =
    if (config.enablePlayHmrcSubmitView) {
      submitTemplate()
    } else {
      views.html.application.submit()
    }

  def presentSubmitted = CSRSecureAppAction(AbleToWithdrawApplicationRole) { implicit request =>
    implicit user =>
      Future.successful(Ok(submittedView()))
  }

  private def submittedView(implicit request: Request[_], user: CachedDataWithApp): Html =
    if (config.enablePlayHmrcSubmittedView) {
      submittedTemplate()
    } else {
      views.html.application.submitted(marketingTrackingEnabled)
    }

  def submit = CSRSecureAppAction(SubmitApplicationRole) { implicit request =>
    implicit user =>
      if (canApplicationBeSubmitted(user.application.overriddenSubmissionDeadline)(user.application.applicationRoute)) {
        applicationClient.submitApplication(user.user.userID, user.application.applicationId).map { _ =>
          Redirect(routes.SubmitApplicationController.presentSubmitted)
        }.recover {
          case _: CannotSubmit => Redirect(routes.PreviewApplicationController.present).flashing(
            danger("error.cannot.submit"))
        }
      } else {
        Future.successful(Redirect(routes.HomeController.present()))
      }
  }
}
