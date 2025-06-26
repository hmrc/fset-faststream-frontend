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
import connectors.SchemeClient.SchemePreferencesNotFound
import connectors.exchange.referencedata.{Scheme, SchemeId}
import connectors.{ReferenceDataClient, SchemeClient}
import forms.SelectedSchemesForm
import helpers.NotificationTypeHelper
import models.ApplicationRoute
import models.page.SelectedSchemesPage
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import security.Roles.SchemesRole
import security.SilhouetteComponent

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SchemePreferencesController @Inject() (
  config: FrontendAppConfig,
  mcc: MessagesControllerComponents,
  val secEnv: SecurityEnvironment,
  val silhouetteComponent: SilhouetteComponent,
  val notificationTypeHelper: NotificationTypeHelper,
  schemeClient: SchemeClient,
  referenceDataClient: ReferenceDataClient
)(implicit val ec: ExecutionContext) extends BaseController(config, mcc) {

  def present: Action[AnyContent] = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      referenceDataClient.allSchemes.flatMap { schemes =>
        val page = SelectedSchemesPage(sortSchemes(schemes))
        val formObj = new SelectedSchemesForm(schemes, cachedData.application.isSdipFaststream)
        val civilServant = cachedData.application.civilServiceExperienceDetails.exists(_.isCivilServant)

        schemeClient.getSchemePreferences(cachedData.application.applicationId).map { selectedSchemes =>
          Ok(views.html.application.schemePreferences.schemeSelection(page, civilServant, formObj.form.fill(selectedSchemes)))
        }.recover {
          case _: SchemePreferencesNotFound =>
            Ok(views.html.application.schemePreferences.schemeSelection(page, civilServant, formObj.form))
        }
      }
  }

  private def sortSchemes(schemes: List[Scheme]) = {
    // Sort the schemes so HoP is last
    val HousesOfParliament = "HousesOfParliament"
    val withoutHop = schemes.filterNot(s => s.id == SchemeId(HousesOfParliament))
    val hop = schemes.filter(s => s.id == SchemeId(HousesOfParliament))
    withoutHop ++ hop
  }

  def submit: Action[AnyContent] = CSRSecureAppAction(SchemesRole) { implicit request =>
    implicit cachedData =>
      referenceDataClient.allSchemes.flatMap { schemes =>
        val isCivilServant = cachedData.application.civilServiceExperienceDetails.exists(_.isCivilServant)
        new SelectedSchemesForm(schemes, cachedData.application.isSdipFaststream).form.bindFromRequest().fold(
          invalidForm => {
            val page = SelectedSchemesPage(sortSchemes(schemes))
            Future.successful(Ok(views.html.application.schemePreferences.schemeSelection(page, isCivilServant, invalidForm)))
          },
          selectedSchemes => {
            val sdip = SchemeId("Sdip")
            val selectedSchemesAmended = cachedData.application.applicationRoute match {
              case ApplicationRoute.SdipFaststream if !selectedSchemes.schemes.contains(sdip.value) => {
                selectedSchemes.copy(schemes = selectedSchemes.schemes :+ sdip.value)
              }
              case _ => selectedSchemes
            }

            for {
              _ <- schemeClient.updateSchemePreferences(selectedSchemesAmended)(cachedData.application.applicationId)
              redirect <- secEnv.userService.refreshCachedUser(cachedData.user.userID).map { _ =>
                Redirect(routes.AssistanceDetailsController.present)
              }
            } yield {
              redirect
            }
          }
        )
      }
  }
}
