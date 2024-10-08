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

import connectors.exchange.GeneralDetails
import connectors.exchange.candidatescores.{AssessmentScoresAllExercises, ExerciseAverageResult}
import mappings.Address
import models.UniqueIdentifier
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.test.Helpers._
import testkit.TestableSecureActions
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.Future

class AssessmentFeedbackControllerSpec extends BaseControllerSpec {

  "presentWithdrawApplication" should {
    "display assessment feedback" in new TestFixture {
      val result = controller.present(applicationId)(fakeRequest)
      status(result) mustBe OK
    }
  }

  trait TestFixture extends BaseControllerTestFixture {
    val applicationId = UniqueIdentifier(UUID.randomUUID().toString)

    class TestableHomeController extends AssessmentFeedbackController(
      mockConfig, stubMcc, mockSecurityEnv, mockSilhouetteComponent, mockNotificationTypeHelper,
      mockAssessmentScoresClient, mockApplicationClient)
      with TestableSecureActions

    //scalastyle:off method.length
    def controller = new TestableHomeController {

      val assessmentScores = AssessmentScoresAllExercises(
        applicationId,
        exercise1 = None,
        exercise2 = None,
        exercise3 = None,
        finalFeedback = None
      )

      val exerciseAverageResult = ExerciseAverageResult(
        exercise1Average = 1.0,
        exercise2Average = 1.0,
        exercise3Average = 1.0,
        overallScore = 3.0)

      val generalDetails = GeneralDetails(
        firstName = "Joe",
        lastName = "BLoggs",
        preferredName = "Joe",
        email = "joe@bloggs.com",
        dateOfBirth = LocalDate.now(),
        outsideUk = false,
        address = Address(line1 = "line1", line2 = None, line3 = None, line4 = None),
        postCode = None,
        fsacIndicator = None,
        country = None,
        phone = None,
        civilServiceExperienceDetails = None,
        edipCompleted = None,
        edipYear = None,
        otherInternshipCompleted = None,
        otherInternshipName = None,
        otherInternshipYear = None,
        updateApplicationStatus = None
      )

      when(mockAssessmentScoresClient.findReviewerAcceptedAssessmentScores(any[UniqueIdentifier])(any[HeaderCarrier]))
        .thenReturn(Future.successful(assessmentScores))
      when(mockApplicationClient.findFsacExerciseAverages(any[UniqueIdentifier])(any[HeaderCarrier]))
        .thenReturn(Future.successful(exerciseAverageResult))
      when(mockApplicationClient.getPersonalDetails(any[UniqueIdentifier], any[UniqueIdentifier])(any[HeaderCarrier]))
        .thenReturn(Future.successful(generalDetails))
    } //scalastyle:on
  }
}
