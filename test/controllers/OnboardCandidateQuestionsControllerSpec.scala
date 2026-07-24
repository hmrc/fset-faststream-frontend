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

import connectors.exchange.OnboardQuestions
import forms.OnboardQuestionsForm
import models.{CachedData, CachedDataWithApp, UniqueIdentifier}
import org.mockito.ArgumentMatchers.{eq as eqTo, *}
import org.mockito.Mockito.*
import play.api.test.Helpers.*
import testkit.MockitoImplicits.*
import testkit.TestableSecureActions
import uk.gov.hmrc.http.HeaderCarrier

class OnboardCandidateQuestionsControllerSpec extends BaseControllerSpec {
  "present" should {
    "display the onboarding questions page" in new TestFixture {
      when(mockOnboardQuestionsClient.findQuestions(any[UniqueIdentifier])(any[HeaderCarrier])).thenReturnAsync(None)

      val result = controller.present(currentApplicationId)(fakeRequest)

      status(result) mustBe OK
      // We are testing against the legacy view!!!
      contentAsString(result) must include ("Onboarding questions | Apply for the Civil Service Fast Stream")
    }
  }

  "submit" should {
    "handle an invalid NI number" in new TestFixture {
      val request = fakeRequest.withMethod("POST").withFormUrlEncodedBody("niNumber" -> "")
      val result = controller.submit(request)

      status(result) mustBe OK
      val content = contentAsString(result)
      // We are testing against the legacy view!!!
      content must include ("<title>Onboarding questions | Apply for the Civil Service Fast Stream")
      content must include ("error.niNumber.wrong.format")
    }

    "handle a valid NI number" in new TestFixture {
      when(mockOnboardQuestionsClient.saveQuestions(any[UniqueIdentifier], any[OnboardQuestions])(any[HeaderCarrier])).thenReturnAsync()

      val request = fakeRequest.withMethod("POST").withFormUrlEncodedBody("niNumber" -> "NR293031B")
      val result = controller.submit(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.HomeController.present().url)
      flash(result).data mustBe Map("success" -> "Successfully saved")
    }
  }

  trait TestFixture extends BaseControllerTestFixture {
    val onboardQuestionsTemplate = mock[views.html.application.onboardCandidateQuestions.OnboardQuestions2]
    val formWrapper = new OnboardQuestionsForm

    implicit val candWithApp: CachedDataWithApp = currentCandidateWithApp

    def controller: OnboardCandidateQuestionsController & TestableSecureActions = new OnboardCandidateQuestionsController(
      mockConfig, stubMcc, onboardQuestionsTemplate, mockSecurityEnv, mockSilhouetteComponent,
      mockNotificationTypeHelper, mockOnboardQuestionsClient, formWrapper
    ) with TestableSecureActions {
      override val candidate: CachedData = CachedData(candWithApp.user, Some(candWithApp.application))
    }
  }
}
