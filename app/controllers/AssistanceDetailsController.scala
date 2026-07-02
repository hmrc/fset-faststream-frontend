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
import forms.AssistanceDetailsForm
import connectors.ApplicationClient
import connectors.ApplicationClient.AssistanceDetailsNotFound

import javax.inject.{Inject, Singleton}
import models.CachedData
import security.SilhouetteComponent
import security.ProgressStatusRoleUtils.*
import security.Roles.AssistanceDetailsRole

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import helpers.NotificationTypeHelper
import play.api.data.Form
import play.twirl.api.Html
import views.html.application.AssistanceDetails2

@Singleton
class AssistanceDetailsController @Inject()(
                                             config: FrontendAppConfig,
                                             mcc: MessagesControllerComponents,
                                             assistanceDetailsTemplate: AssistanceDetails2,
                                             val secEnv: SecurityEnvironment,
                                             val silhouetteComponent: SilhouetteComponent,
                                             val notificationTypeHelper: NotificationTypeHelper,
                                             applicationClient: ApplicationClient,
                                             formWrapper: AssistanceDetailsForm)(implicit val ec: ExecutionContext)
  extends BaseController(config, mcc) {

  def present: Action[AnyContent] = CSRSecureAppAction(AssistanceDetailsRole) { implicit request =>
    implicit user =>
      applicationClient.getAssistanceDetails(user.user.userID, user.application.applicationId).map { ad =>
        val form = formWrapper.form.fill(AssistanceDetailsForm.Data(ad))
        Ok(assistanceDetailsView(form))
      }.recover {
        case _: AssistanceDetailsNotFound => Ok(assistanceDetailsView(formWrapper.form))
      }
  }

  private def assistanceDetailsView(form: Form[AssistanceDetailsForm.Data])(
    implicit request: Request[_], user: Option[models.CachedData]): Html =
    if (config.enablePlayHmrcAssistanceDetailsView) {
      assistanceDetailsTemplate(form, AssistanceDetailsForm.disabilityCategoriesList)
    } else {
      views.html.application.assistanceDetails(form, AssistanceDetailsForm.disabilityCategoriesList)
    }

  def submit: Action[AnyContent] = CSRSecureAppAction(AssistanceDetailsRole) { implicit request =>
    implicit user =>
      formWrapper.form.bindFromRequest().fold(
        invalidForm =>
          Future.successful(Ok(assistanceDetailsView(invalidForm))),
        data => {
          applicationClient.updateAssistanceDetails(
            user.application.applicationId, user.user.userID, data.sanitizeData.exchange).map { _ =>
            if (hasOccupation(CachedData(user.user, Some(user.application)))) {
              Redirect(routes.PreviewApplicationController.present)
            } else {
              Redirect(routes.QuestionnaireController.presentStartOrContinue)
            }
          }
        }
      )
  }
}
