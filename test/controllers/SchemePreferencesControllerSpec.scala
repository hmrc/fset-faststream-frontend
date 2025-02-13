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
import connectors.SchemeClient.SchemePreferencesNotFound
import connectors.exchange.SchemePreferencesExamples
import forms.SelectedSchemesForm._
import models._
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import play.api.test.Helpers._
import testkit.MockitoImplicits._
import testkit.{TestableSecureActionsWithAuthorizationForFsCandidate, TestableSecureActionsWithAuthorizationForSdipCandidate}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class SchemePreferencesControllerSpec extends BaseControllerSpec {

  "present" should {
    "allow a Faststream candidate access to the scheme selection page" in new TestFixture {
      when(mockSchemeClient.getSchemePreferences(any[UniqueIdentifier])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new SchemePreferencesNotFound))

      val result = fsController.present(fakeRequest)

      status(result) mustBe OK
      val content = contentAsString(result)
      content must include ("Choose your schemes")
      content must include (s"""name="scheme_0" value=''""")
      content must include (s"""name="scheme_1" value=''""")
      content must include (s"""name="scheme_2" value=''""")
      content must include (s"""name="scheme_3" value=''""")
      content must include (s"""name="scheme_4" value=''""")
    }

    "not allow a Sdip candidate access to the Faststream scheme selection page" in new TestFixture {
      val result = sdipController.present(fakeRequest)

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(routes.HomeController.present().url)
      flash(result).data mustBe Map("danger" -> "access.denied")
    }

    "populate selected schemes for the candidate" in new TestFixture {
      when(mockSchemeClient.getSchemePreferences(any[UniqueIdentifier])(any[HeaderCarrier]))
        .thenReturn(Future.successful(SchemePreferencesExamples.DefaultSelectedSchemes))

      val result = fsController.present(fakeRequest)

      status(result) mustBe OK
      val content = contentAsString(result)
      content must include ("Choose your schemes")
      content must include (s"""name="scheme_0" value='Finance'""")
      content must include (s"""name="scheme_1" value='Commercial'""")
      content must include (s"""name="scheme_2" value=''""")
      content must include (s"""name="scheme_3" value=''""")
      content must include (s"""name="scheme_4" value=''""")
    }
  }

  "submit scheme preferences" should {
    "update scheme preferences details" in new TestFixture {
      val request = fakeRequest.withMethod("POST")
        .withFormUrlEncodedBody("scheme_0" -> "Finance", "scheme_1" -> "Commercial", "orderAgreed" -> "true", "eligible" -> "true")
      val schemePreferences = SchemePreferences(List("Finance", "Commercial"), orderAgreed = true, eligible = true)
      when(mockSchemeClient.updateSchemePreferences(eqTo(schemePreferences))(any[UniqueIdentifier])(any[HeaderCarrier])).thenReturnAsync()

      val result = fsController.submit(request)

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
    when(mockReferenceDataClient.allSchemes(any[HeaderCarrier])).thenReturnAsync(ReferenceDataExamples.Schemes.AllSchemes)

    def fsController = new SchemePreferencesController(mockConfig,
      stubMcc, mockSecurityEnv, mockSilhouetteComponent, mockNotificationTypeHelper,
      mockSchemeClient, mockReferenceDataClient) with TestableSecureActionsWithAuthorizationForFsCandidate

    def sdipController = new SchemePreferencesController(mockConfig,
      stubMcc, mockSecurityEnv, mockSilhouetteComponent, mockNotificationTypeHelper,
      mockSchemeClient, mockReferenceDataClient) with TestableSecureActionsWithAuthorizationForSdipCandidate
  }
}
