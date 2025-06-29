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

import com.github.tomakehurst.wiremock.client.WireMock.any as _
import connectors.ApplicationClient.CannotWithdraw
import connectors.ReferenceDataExamples
import connectors.exchange.*
import connectors.exchange.candidateevents.CandidateAllocationWithEvent
import connectors.exchange.referencedata.SchemeId
import forms.{SchemeWithdrawForm, WithdrawApplicationForm, WithdrawApplicationFormExamples}
import models.*
import models.ApplicationRoute.*
import models.SecurityUserExamples.*
import models.events.AllocationStatuses
import org.mockito.ArgumentMatchers.{eq as eqTo, *}
import org.mockito.Mockito.*
import play.api.mvc.Request
import play.api.test.Helpers.*
import testkit.MockitoImplicits.*
import testkit.TestableSecureActions
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class WithdrawControllerSpec extends BaseControllerSpec {

  // The current candidate needed by other methods than withdraw may be different, in that case, we might need
  // to split to tests of this file.
  // This is the implicit user
  override def currentCandidateWithApp: CachedDataWithApp = {
    CachedDataWithApp(ActiveCandidate.user,
      CachedDataExample.SubmittedApplication.copy(userId = ActiveCandidate.user.userID))
  }

  "presentWithdrawApplication" should {
    "display withdraw page" in new TestFixture {
      val result = controller.presentWithdrawApplication()(fakeRequest)
      status(result) mustBe OK
      val content = contentAsString(result)
      content must include("<title>Withdraw your application")
      content must include(s"""<span class="your-name" id="bannerUserName">${currentCandidate.user.preferredName.get}</span>""")
    }
  }

  "presentWithdrawScheme" should {
    "redirect to withdraw application if the candidate only has 1 Green scheme remaining" in new TestFixture {
      when(mockApplicationClient.getCurrentSchemeStatus(eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturnAsync(
          List(SchemeEvaluationResultWithFailureDetails(SchemeId("DiplomaticAndDevelopment"), SchemeStatus.Green))
        )

      val result = controller.presentWithdrawScheme()(fakeRequest)
      status(result) mustBe SEE_OTHER
      flash(result).get("warning") mustBe Some("withdraw.scheme.last")
      // This varies when you run this test in isolation vs all tests together
      redirectLocation(result).getOrElse("").endsWith("/application/withdraw") mustBe true
    }

    "redirect to withdraw application if the candidate only has 1 Amber scheme remaining" in new TestFixture {
      when(mockApplicationClient.getCurrentSchemeStatus(eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturnAsync(
          List(SchemeEvaluationResultWithFailureDetails(SchemeId("DiplomaticAndDevelopment"), SchemeStatus.Amber))
        )

      val result = controller.presentWithdrawScheme()(fakeRequest)
      status(result) mustBe SEE_OTHER
      flash(result).get("warning") mustBe Some("withdraw.scheme.last")
      // This varies when you run this test in isolation vs all tests together
      redirectLocation(result).getOrElse("").endsWith("/application/withdraw") mustBe true
    }

    "prevent the candidate from withdrawing if they only have Red or Withdrawn schemes" in new TestFixture {
      when(mockApplicationClient.getCurrentSchemeStatus(eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturnAsync(
          List(
            SchemeEvaluationResultWithFailureDetails(SchemeId("DiplomaticAndDevelopment"), SchemeStatus.Red),
            SchemeEvaluationResultWithFailureDetails(SchemeId("Finance"), SchemeStatus.Withdrawn)
          )
        )

      val result = controller.presentWithdrawScheme()(fakeRequest)
      status(result) mustBe SEE_OTHER
      flash(result).get("danger") mustBe Some("access.denied")
      // This varies when you run this test in isolation vs all tests together
      redirectLocation(result).getOrElse("").endsWith("/dashboard") mustBe true
    }

    "display withdraw scheme page when candidate has at least 2 schemes that are Green or Amber" in new TestFixture {
      when(mockApplicationClient.getCurrentSchemeStatus(eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturnAsync(
          List(
            SchemeEvaluationResultWithFailureDetails(SchemeId("DiplomaticAndDevelopment"), SchemeStatus.Green),
            SchemeEvaluationResultWithFailureDetails(SchemeId("Finance"), SchemeStatus.Amber),
            SchemeEvaluationResultWithFailureDetails(SchemeId("Commercial"), SchemeStatus.Withdrawn)
          )
        )

      val result = controller.presentWithdrawScheme()(fakeRequest)
      status(result) mustBe OK
      val content = contentAsString(result)
      content must include("Withdraw from a scheme")
    }
  }

  "withdrawApplication" should {
    "display withdraw form when the form was submitted invalid" in new TestFixture {
      val Request = fakeRequest.withMethod("POST")
        .withFormUrlEncodedBody(WithdrawApplicationFormExamples.OtherReasonInvalidNoReasonFormUrlEncodedBody: _*)

      val result = controller.withdrawApplication(Request)

      status(result) mustBe OK
      val content = contentAsString(result)
      content must include(routes.WithdrawController.withdrawApplication.url)
      content must include ("error.reason.required")
    }

    "display dashboard with error message when form is valid but cannot withdraw" in new TestFixture {
      val Request = fakeRequest.withMethod("POST").withFormUrlEncodedBody(WithdrawApplicationFormExamples.ValidFormUrlEncodedBody: _*)
      when(mockApplicationClient.withdrawApplication(eqTo(currentApplicationId),
        eqTo(WithdrawApplicationExamples.Valid))(any[HeaderCarrier])).thenReturn(Future.failed(new CannotWithdraw))

      val result = controller.withdrawApplication()(Request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) must be(Some(routes.HomeController.present().url))
      flash(result).data mustBe Map("danger" -> "error.cannot.withdraw")
    }

    "display dashboard with withdrawn success message when withdraw is successful" in new TestFixture {
      val Request = fakeRequest.withMethod("POST").withFormUrlEncodedBody(WithdrawApplicationFormExamples.ValidFormUrlEncodedBody: _*)
      when(mockApplicationClient.withdrawApplication(eqTo(currentApplicationId),
        eqTo(WithdrawApplicationExamples.Valid))(any[HeaderCarrier])).thenReturn(Future.successful(()))
      when(mockApplicationClient.getApplicationProgress(eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(ProgressResponseExamples.Submitted))

      val result = controller.withdrawApplication()(Request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.HomeController.present().url)
      val expectedValue: Map[String, String] = Map("success" -> ("application.withdrawn"))
      flash(result).data mustBe expectedValue
    }
  }

  trait TestFixture extends BaseControllerTestFixture {
    def controller(implicit candWithApp: CachedDataWithApp = currentCandidateWithApp,
                   appRouteState: ApplicationRouteState = defaultApplicationRouteState)
    = {
      val CandidateExample = CachedData(candWithApp.user, Some(candWithApp.application))

      val withdrawFormWrapper = new WithdrawApplicationForm
      val schemeWithdrawFormWrapper = new SchemeWithdrawForm

      when(mockReferenceDataClient.allSchemes(any[HeaderCarrier])).thenReturnAsync(ReferenceDataExamples.Schemes.AllSchemes)
      when(mockUserService.refreshCachedUser(any[UniqueIdentifier])(any[HeaderCarrier], any[Request[_]]))
        .thenReturn(Future.successful(CachedData(ActiveCandidate.user, Some(CreatedApplication.copy(userId = ActiveCandidate.user.userID)))))

      when(mockApplicationClient.getAssistanceDetails(eqTo(currentUserId), eqTo(currentApplicationId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(AssistanceDetailsExamples.OnlyDisabilityNoGisNoAdjustments))
//      when(mockApplicationClient.getPhase1TestProfile(eqTo(currentApplicationId))(any[HeaderCarrier]))
//        .thenReturn(Future.failed(new OnlineTestNotFound))

      mockPostOnlineTestsDashboardCalls()

        when(mockUserService.refreshCachedUser(any[UniqueIdentifier])(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(Future.successful(CandidateExample))

      def mockPostOnlineTestsDashboardCalls(hasAnalysisExerciseAlready: Boolean = false) = {
        val alloc = CandidateAllocationWithEvent("", "", AllocationStatuses.CONFIRMED, EventsExamples.Event1)
        when(mockApplicationClient.candidateAllocationEventWithSession(any[UniqueIdentifier])(any[HeaderCarrier]())).thenReturnAsync(List(alloc))
        when(mockApplicationClient.hasAnalysisExercise(any[UniqueIdentifier]())(any[HeaderCarrier])).thenReturnAsync(hasAnalysisExerciseAlready)
      }

      new WithdrawController(mockConfig, stubMcc, mockSecurityEnv, mockSilhouetteComponent,
        mockNotificationTypeHelper, mockApplicationClient, mockReferenceDataClient, withdrawFormWrapper, schemeWithdrawFormWrapper)
        with TestableSecureActions {
        override val candidate: CachedData = CandidateExample
        override val candidateWithApp: CachedDataWithApp = candWithApp
        override val appRouteConfigMap = Map(Faststream -> appRouteState, Edip -> appRouteState, Sdip -> appRouteState)
      }
    }
  }
}
