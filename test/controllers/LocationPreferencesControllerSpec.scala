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

import connectors.ReferenceDataExamples
import connectors.SdipLocationsClient.LocationPreferencesNotFound
import connectors.exchange.SelectedLocations
import models._
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import play.api.test.Helpers._
import testkit.MockitoImplicits._
import testkit.{TestableSecureActionsWithAuthorizationForFsCandidate, TestableSecureActionsWithAuthorizationForSdipCandidate}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class LocationPreferencesControllerSpec extends BaseControllerSpec {

  "present" should {
    "allow a Sdip candidate access to the location selection page" in new TestFixture {
      when(mockSdipLocationsClient.getLocationPreferences(any[UniqueIdentifier])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new LocationPreferencesNotFound))

      val result = sdipController.present(fakeRequest)

      status(result) mustBe OK
      val content = contentAsString(result)
      content must include("Choose your preferred locations")
      content must include(s"""name="location_0" value=''""")
      content must include(s"""name="location_1" value=''""")
      content must include(s"""name="location_2" value=''""")
      content must include(s"""name="location_3" value=''""")
      content must include(s"""name="location_4" value=''""")
    }

    "not allow a Faststream candidate access to the Sdip location selection page" in new TestFixture {
      val result = fsController.present(fakeRequest)

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(routes.HomeController.present().url)
      flash(result).data mustBe Map("danger" -> "access.denied")
    }

    "populate selected locations and interests for the candidate" in new TestFixture {
      when(mockSdipLocationsClient.getLocationPreferences(any[UniqueIdentifier])(any[HeaderCarrier]))
        .thenReturn(Future.successful(SelectedLocations(List("London", "Manchester"), List("Cyber"))))

      val result = sdipController.present(fakeRequest)

      status(result) mustBe OK
      val content = contentAsString(result)
      content must include ("Choose your preferred locations")
      content must include (s"""name="location_0" value='London'""")
      content must include (s"""name="location_1" value='Manchester'""")
      content must include (s"""name="location_2" value=''""")
      content must include (s"""name="location_3" value=''""")
      content must include (s"""name="location_4" value=''""")
      //scalastyle:off line.size.limit
      content must include ("""value="Cyber"  checked="checked"""")
      //scalastyle:on
    }
  }

  "submit location preferences" should {
    "update location preference details" in new TestFixture {
      val request = fakeRequest.withMethod("POST")
        .withFormUrlEncodedBody("location_0" -> "London", "location_1" -> "Manchester", "interests[0]" -> "Cyber")
      val selectedLocations = SelectedLocations(List("London", "Manchester"), List("Cyber"))
      when(mockSdipLocationsClient.updateLocationPreferences(eqTo(selectedLocations))(any[UniqueIdentifier])(any[HeaderCarrier]))
        .thenReturnAsync()

      val result = sdipController.submit(request)

      print(contentAsString(result))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.AssistanceDetailsController.present.url)
    }
  }

  trait TestFixture extends BaseControllerTestFixture {
    when(mockUserService.refreshCachedUser(any[UniqueIdentifier])(any[HeaderCarrier], any())).thenReturn(Future.successful(CachedData(
      mock[CachedUser],
      application = Some(mock[ApplicationData])
    )))
    when(mockReferenceDataClient.sdipLocations(any[HeaderCarrier])).thenReturnAsync(ReferenceDataExamples.Locations.AllLocations)

    def sdipController = new LocationPreferencesController(mockConfig,
      stubMcc, mockSecurityEnv, mockSilhouetteComponent, mockNotificationTypeHelper,
      mockSdipLocationsClient, mockReferenceDataClient) with TestableSecureActionsWithAuthorizationForSdipCandidate

    def fsController = new LocationPreferencesController(mockConfig,
      stubMcc, mockSecurityEnv, mockSilhouetteComponent, mockNotificationTypeHelper,
      mockSdipLocationsClient, mockReferenceDataClient) with TestableSecureActionsWithAuthorizationForFsCandidate
  }
}
