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
import connectors.SdipLocationsClient.LocationPreferencesNotFound
import connectors.{ReferenceDataClient, SdipLocationsClient}
import forms.SelectedLocationsForm
import helpers.NotificationTypeHelper
import models.page.SelectedLocationsPage
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import security.Roles.LocationsRole
import security.SilhouetteComponent
import views.html.application.locationPreferences.LocationSelection2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LocationPreferencesController @Inject()(
                                               config: FrontendAppConfig,
                                               mcc: MessagesControllerComponents,
                                               locationSelectionTemplate: LocationSelection2,
                                               val secEnv: SecurityEnvironment,
                                               val silhouetteComponent: SilhouetteComponent,
                                               val notificationTypeHelper: NotificationTypeHelper,
                                               locationClient: SdipLocationsClient,
                                               referenceDataClient: ReferenceDataClient
)(implicit val ec: ExecutionContext) extends BaseController(config, mcc) {

  def present: Action[AnyContent] = CSRSecureAppAction(LocationsRole) { implicit request =>
    implicit cachedData =>
      referenceDataClient.sdipLocations.flatMap { locations =>
        val page = SelectedLocationsPage(locations)
        val formObj = new SelectedLocationsForm(locations)

        locationClient.getLocationPreferences(cachedData.application.applicationId).map { selectedLocations =>
          Ok(locationSelectionView(page, formObj.form.fill(selectedLocations), SelectedLocationsForm.interestsList))
        }.recover {
          case _: LocationPreferencesNotFound =>
            Ok(locationSelectionView(page, formObj.form, SelectedLocationsForm.interestsList))
        }
      }
  }

  private def locationSelectionView(page: SelectedLocationsPage,
                                  form: Form[forms.SelectedLocationsForm.LocationPreferences],
                                  interests: List[String])(
                                   implicit request: Request[_], user: Option[models.CachedData]): Html =
    if (config.enablePlayHmrcLocationSelectionView) {
      locationSelectionTemplate(page, form, interests)
    } else {
      views.html.application.locationPreferences.locationSelection(page, form, interests)
    }

  def submit: Action[AnyContent] = CSRSecureAppAction(LocationsRole) { implicit request =>
    implicit cachedData =>
      referenceDataClient.sdipLocations.flatMap { locations =>
        new SelectedLocationsForm(locations).form.bindFromRequest().fold(
          invalidForm => {
            val page = SelectedLocationsPage(locations)
            Future.successful(Ok(locationSelectionView(page, invalidForm, SelectedLocationsForm.interestsList)))
          },
          selectedSchemes => {
            for {
              _ <- locationClient.updateLocationPreferences(selectedSchemes)(cachedData.application.applicationId)
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
