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
import connectors.ApplicationClient.CannotUpdateRecord2
import connectors.OnboardQuestionsClient
import connectors.exchange.OnboardQuestions
import forms.OnboardQuestionsForm
import helpers.NotificationTypeHelper
import models.UniqueIdentifier
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import security.Roles.ActiveUserRole
import security.SilhouetteComponent
import views.html.application.onboardCandidateQuestions.OnboardQuestions2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OnboardCandidateQuestionsController @Inject()(
                                                     config: FrontendAppConfig,
                                                     mcc: MessagesControllerComponents,
                                                     onboardQuestionsTemplate: OnboardQuestions2,
                                                     val secEnv: SecurityEnvironment,
                                                     val silhouetteComponent: SilhouetteComponent,
                                                     val notificationTypeHelper: NotificationTypeHelper,
                                                     onboardQuestionsClient: OnboardQuestionsClient,
                                                     formWrapper: OnboardQuestionsForm)(
  implicit val ec: ExecutionContext) extends BaseController(config, mcc) {

  import notificationTypeHelper.*

  def present(applicationId: UniqueIdentifier): Action[AnyContent] = CSRSecureAppAction(ActiveUserRole) {
    implicit request =>
      implicit cachedData =>
        for {
          onboardQuestionsOpt <- onboardQuestionsClient.findQuestions(applicationId)
        } yield {
          Ok(onboardQuestionsView(formWrapper.form, onboardQuestionsOpt.isDefined))
        }
  }

  private def onboardQuestionsView(form: Form[OnboardQuestionsForm.Data], dataSaved: Boolean)(
    implicit request: Request[_], user: Option[models.CachedData]): Html =
    if (config.enablePlayHmrcOnboardQuestionsView) {
      onboardQuestionsTemplate(form, dataSaved)
    } else {
      views.html.application.onboardCandidateQuestions.onboardQuestions(form, dataSaved)
    }

  def submit: Action[AnyContent] = CSRSecureAppAction(ActiveUserRole) { implicit request =>
    implicit user =>
      formWrapper.form.bindFromRequest().fold(
        invalidForm =>
          Future.successful(Ok(onboardQuestionsView(invalidForm, dataSaved = false))),
        data => {
          (for {
            _ <- onboardQuestionsClient.saveQuestions(user.application.applicationId, OnboardQuestions(data.niNumber))
          } yield {
            Redirect(routes.HomeController.present()).flashing(success("Successfully saved"))
          }).recover {
            case ex: CannotUpdateRecord2 =>
              logger.error(s"Error occurred saving onboard questions for candidate ${user.application.applicationId}: ${ex.getMessage}")
              Redirect(routes.HomeController.present()).flashing(danger("An error occurred whilst saving the onboarding questions"))
          }
        }
      )
  }
}
