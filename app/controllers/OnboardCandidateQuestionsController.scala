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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import security.Roles.ActiveUserRole
import security.SilhouetteComponent
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OnboardCandidateQuestionsController @Inject()(
  config: FrontendAppConfig,
  mcc: MessagesControllerComponents,
  val secEnv: SecurityEnvironment,
  val silhouetteComponent: SilhouetteComponent,
  val notificationTypeHelper: NotificationTypeHelper,
  onboardQuestionsClient: OnboardQuestionsClient,
  formWrapper: OnboardQuestionsForm)(implicit val ec: ExecutionContext) extends BaseController(config, mcc) {

  import notificationTypeHelper.*

  def present(applicationId: UniqueIdentifier): Action[AnyContent] = CSRSecureAction(ActiveUserRole) {
    implicit request =>
      implicit cachedData =>
        for {
          onboardQuestionsOpt <- onboardQuestionsClient.findQuestions(applicationId)
        } yield {
          Ok(views.html.application.onboardCandidateQuestions.onboardQuestions(formWrapper.form, onboardQuestionsOpt.isDefined))
        }
  }

  private def findQuestions(appId: UniqueIdentifier)(implicit hc: HeaderCarrier) = {
    val res = onboardQuestionsClient.findQuestions(appId)
    res.map {
      case Some(onboardQuestions) =>
        OnboardQuestionsForm.Data(niNumber = onboardQuestions.niNumber)
      case _ =>
        OnboardQuestionsForm.Data(niNumber = "")
    }
  }

  def submit: Action[AnyContent] = CSRUserAwareAction { implicit request =>
    implicit user =>
      formWrapper.form.bindFromRequest().fold(
        invalidForm => Future.successful(Ok(views.html.application.onboardCandidateQuestions.onboardQuestions(invalidForm, dataSaved = false))),
        data => {
          val applicationData = user.head.application.getOrElse(throw new RuntimeException("No cached user data found"))
          (for {
            _ <- onboardQuestionsClient.saveQuestions(applicationData.applicationId, OnboardQuestions(data.niNumber))
          } yield {
            Redirect(routes.HomeController.present()).flashing(success("Successfully saved"))
          }).recover {
            case ex: CannotUpdateRecord2 =>
              logger.error(s"Error occurred saving onboard questions for candidate ${applicationData.applicationId}: ${ex.getMessage}")
              Redirect(routes.HomeController.present()).flashing(danger("An error occurred whilst saving the onboarding questions"))
          }
        }
      )
  }
}
