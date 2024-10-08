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

package connectors

import config.{CSRHttp, FrontendAppConfig}
import connectors.UserManagementClient.TokenEmailPairInvalidException
import connectors.exchange.GeneralDetails._
import connectors.exchange.Questionnaire._
import connectors.exchange._
import connectors.exchange.campaignmanagement.AfterDeadlineSignupCodeUnused
import connectors.exchange.candidateevents.{CandidateAllocationWithEvent, CandidateAllocations}
import connectors.exchange.candidatescores.ExerciseAverageResult
import connectors.exchange.sift.SiftState
import models.{Adjustments, ApplicationRoute, UniqueIdentifier}
import play.api.http.Status._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// scalastyle:off number.of.methods
@Singleton
class ApplicationClient @Inject() (config: FrontendAppConfig, http: CSRHttp)(implicit ec: ExecutionContext)
  extends TestDataGeneratorClient(config, http) {

  import ApplicationClient._

  val apiBaseUrl: String = config.faststreamBackendConfig.url.host + config.faststreamBackendConfig.url.base

  def afterDeadlineSignupCodeUnusedAndValid(afterDeadlineSignupCode: String)(implicit hc: HeaderCarrier)
  : Future[AfterDeadlineSignupCodeUnused] = {
    http.GET[AfterDeadlineSignupCodeUnused](s"$apiBaseUrl/campaign-management/afterDeadlineSignupCodeUnusedAndValid",
      Seq("code" -> afterDeadlineSignupCode))
  }

  def createApplication(userId: UniqueIdentifier, frameworkId: String,
    applicationRoute: ApplicationRoute.ApplicationRoute = ApplicationRoute.Faststream)
    (implicit hc: HeaderCarrier): Future[ApplicationResponse] = {
    http.PUT[CreateApplicationRequest, ApplicationResponse](s"$apiBaseUrl/application/create",
      CreateApplicationRequest(userId, frameworkId, applicationRoute)
    )
  }

  def overrideSubmissionDeadline(applicationId: UniqueIdentifier, overrideRequest: OverrideSubmissionDeadlineRequest)(
    implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[OverrideSubmissionDeadlineRequest, HttpResponse](
      s"$apiBaseUrl/application/overrideSubmissionDeadline/$applicationId",
      overrideRequest).map { response =>
        if (response.status != OK) { throw new CannotSubmitOverriddenSubmissionDeadline } else { () }
      }
  }

  def markSignupCodeAsUsed(code: String, applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.GET[HttpResponse](
      s"$apiBaseUrl/application/markSignupCodeAsUsed",
      Seq("code" -> code, "applicationId" -> applicationId.toString)
    ).map { response =>
      if (response.status != OK) {
        throw new CannotMarkSignupCodeAsUsed(applicationId.toString, code)
      } else {
        ()
      }
    }
  }

  def submitApplication(userId: UniqueIdentifier, applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[String, Either[UpstreamErrorResponse, Unit]](s"$apiBaseUrl/application/submit/$userId/$applicationId", "").map {
      case Right(_) => ()
      case Left(badRequestEx) if badRequestEx.statusCode == BAD_REQUEST => throw new CannotSubmit
      case Left(ex) => throw ex
    }
  }

  def withdrawApplication(applicationId: UniqueIdentifier, reason: WithdrawApplication)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[WithdrawApplication, Either[UpstreamErrorResponse, Unit]](s"$apiBaseUrl/application/withdraw/$applicationId", reason).map {
      case Right(_) => ()
      case Left(notFoundEx) if notFoundEx.statusCode == NOT_FOUND => throw new CannotWithdraw()
      case Left(ex) => throw ex
    }
  }

  def withdrawScheme(applicationId: UniqueIdentifier, withdrawal: WithdrawScheme)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[WithdrawScheme, Either[UpstreamErrorResponse, Unit]](s"$apiBaseUrl/application/$applicationId/scheme/withdraw", withdrawal)
      .map {
        case Right(_) => ()
        case Left(forbiddenEx) if forbiddenEx.statusCode == FORBIDDEN => throw new SiftExpired
        case Left(_) => throw new CannotWithdraw
      }
  }

  def addReferral(userId: UniqueIdentifier, referral: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[AddReferral, HttpResponse](s"$apiBaseUrl/media/create", AddReferral(userId, referral)).map {
      case response if response.status == CREATED => ()
      case response if response.status == BAD_REQUEST => throw new CannotAddReferral
    }
  }

  // TODO: looks like this is only called by test classes
  def getApplicationProgress(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[ProgressResponse] = {
    http.GET[ProgressResponse](s"$apiBaseUrl/application/progress/$applicationId")
  }

  def findApplication(userId: UniqueIdentifier, frameworkId: String)(implicit hc: HeaderCarrier): Future[ApplicationResponse] = {
    http.GET[ApplicationResponse](s"$apiBaseUrl/application/find/user/$userId/framework/$frameworkId").recover {
      case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new ApplicationNotFound
    }
  }

  def findFsacExerciseAverages(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[ExerciseAverageResult] = {
    http.GET[ExerciseAverageResult](s"$apiBaseUrl/application/$applicationId/fsacExerciseAverages").recover {
      case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND =>
        val msg = s"Found no fsac exercise averages for application id: $applicationId"
        throw new FsacExerciseAveragesNotFound(msg)
    }
  }

  def updatePersonalDetails(applicationId: UniqueIdentifier, userId: UniqueIdentifier, personalDetails: GeneralDetails)
                           (implicit hc: HeaderCarrier): Future[Unit] = {
    http.POST[GeneralDetails, HttpResponse](s"$apiBaseUrl/personal-details/$userId/$applicationId", personalDetails).map {
      case resp if resp.status == CREATED => ()
      case resp if resp.status == BAD_REQUEST => throw new CannotUpdateRecord
    }
  }

  def getPersonalDetails(userId: UniqueIdentifier, applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[GeneralDetails] = {
    http.GET[GeneralDetails](s"$apiBaseUrl/personal-details/$userId/$applicationId").recover {
      case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new PersonalDetailsNotFound
    }
  }

  def updateAssistanceDetails(applicationId: UniqueIdentifier, userId: UniqueIdentifier, assistanceDetails: AssistanceDetails)
                             (implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[AssistanceDetails, HttpResponse](s"$apiBaseUrl/assistance-details/$userId/$applicationId", assistanceDetails).map {
      case resp if resp.status == CREATED => ()
      case resp if resp.status == BAD_REQUEST => throw new CannotUpdateRecord
    }
  }

  def getAssistanceDetails(userId: UniqueIdentifier, applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[AssistanceDetails] = {
    http.GET[AssistanceDetails](s"$apiBaseUrl/assistance-details/$userId/$applicationId")
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new AssistanceDetailsNotFound
      }
  }

  def updateQuestionnaire(applicationId: UniqueIdentifier, sectionId: String, questionnaire: Questionnaire)
                         (implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[Questionnaire, HttpResponse](s"$apiBaseUrl/questionnaire/$applicationId/$sectionId", questionnaire).map {
      case resp if resp.status == ACCEPTED => ()
      case resp if resp.status == BAD_REQUEST => throw new CannotUpdateRecord
    }
  }

  def updatePreview(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[PreviewRequest, HttpResponse](
      s"$apiBaseUrl/application/preview/$applicationId",
      PreviewRequest(true)
    ).map {
      case resp if resp.status == OK => ()
      case resp if resp.status == BAD_REQUEST => throw new CannotUpdateRecord
    }
  }

  def verifyInvigilatedToken(email: String, token: String)(implicit hc: HeaderCarrier): Future[InvigilatedTestUrl] = {
    http.POST[VerifyInvigilatedTokenUrlRequest, InvigilatedTestUrl](
      s"$apiBaseUrl/online-test/phase2/verifyAccessCode",
      VerifyInvigilatedTokenUrlRequest(email.toLowerCase, token))
      .recover {
        case notFoundEx: UpstreamErrorResponse if notFoundEx.statusCode == NOT_FOUND => throw new TokenEmailPairInvalidException()
        case forbidenEx: UpstreamErrorResponse if forbidenEx.statusCode == FORBIDDEN => throw new TestForTokenExpiredException()
      }
  }

  // psi code start
  def getPhase1Tests(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Seq[PsiTest]] = {
    http.GET[Seq[PsiTest]](s"$apiBaseUrl/phase1-tests/$appId").recover {
      case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new OnlineTestNotFound
    }
  }

  def getPhase1TestProfile(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Phase1TestGroupWithNames] = {
    http.GET[Phase1TestGroupWithNames](s"$apiBaseUrl/online-test/psi/phase1/candidate/$appId").recover {
      case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new OnlineTestNotFound
    }
  }

  def getPhase2TestProfile(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Phase2TestGroupWithActiveTest] = {
    http.GET[Phase2TestGroupWithActiveTest](s"$apiBaseUrl/online-test/phase2/candidate/$appId").recover {
      case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new OnlineTestNotFound
    }
  }

  def getPhase1TestGroupWithNamesByOrderId(orderId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Phase1TestGroupWithNames] = {
    http.GET[Phase1TestGroupWithNames](s"$apiBaseUrl/online-test/phase1/candidate/orderId/$orderId")
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new OnlineTestNotFound
      }
  }

  def getPhase2TestProfileByOrderId(orderId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Phase2TestGroupWithActiveTest] = {
    http.GET[Phase2TestGroupWithActiveTest](s"$apiBaseUrl/online-test/phase2/candidate/orderId/$orderId").recover {
      case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new OnlineTestNotFound
    }
  }

  def completeTestByOrderId(orderId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[String, HttpResponse](s"$apiBaseUrl/psi/$orderId/complete", "").map(_ => ())
  }
  // psi code end

  def getPhase3TestGroup(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Phase3TestGroup] = {
    http.GET[Phase3TestGroup](s"$apiBaseUrl/phase3-test-group/$appId").recover {
      case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new OnlineTestNotFound
    }
  }

  def getPhase3Results(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[List[SchemeEvaluationResult]]] = {
    http.GET[Option[List[SchemeEvaluationResult]]](s"$apiBaseUrl/application/$appId/phase3/results")
  }

  def getSiftTestGroup(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[SiftTestGroupWithActiveTest] = {
    http.GET[SiftTestGroupWithActiveTest](s"$apiBaseUrl/psi/sift-test-group/$appId").recover {
      case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new SiftTestNotFound(s"No sift test group found for $appId")
    }
  }

  def getSiftState(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[SiftState]] = {
    http.GET[Option[SiftState]](s"$apiBaseUrl/sift-candidate/state/$appId")
  }

  def getSiftResults(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[List[SchemeEvaluationResult]]] = {
    http.GET[Option[List[SchemeEvaluationResult]]](s"$apiBaseUrl/application/$appId/sift/results")
  }

  def getCurrentSchemeStatus(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Seq[SchemeEvaluationResultWithFailureDetails]] = {
    http.GET[Seq[SchemeEvaluationResultWithFailureDetails]](s"${url.host}${url.base}/application/$appId/currentSchemeStatus")
  }

  private def encodeUrlParam(str: String) = URLEncoder.encode(str, "UTF-8")

  def startPhase3TestByToken(launchpadInviteId: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[String, HttpResponse](s"$apiBaseUrl/launchpad/${encodeUrlParam(launchpadInviteId)}/markAsStarted", "").map(_ => ())
  }

  def completePhase3TestByToken(launchpadInviteId: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[String, HttpResponse](s"$apiBaseUrl/launchpad/${encodeUrlParam(launchpadInviteId)}/markAsComplete", "").map(_ => ())
  }

  def startTest(orderId: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[String, HttpResponse](s"$apiBaseUrl/psi/$orderId/start", "").map(_ => ())
  }

  def startSiftTest(orderId: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[String, HttpResponse](s"$apiBaseUrl/psi/sift-test/$orderId/start", "").map(_ => ())
  }

  def confirmAllocation(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.POST[String, HttpResponse](s"$apiBaseUrl/allocation-status/confirm/$appId", "").map(_ => ())
  }

  def candidateAllocationEventWithSession(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[List[CandidateAllocationWithEvent]] = {
    http.GET[List[CandidateAllocationWithEvent]](
      s"$apiBaseUrl/candidate-allocations/sessions/findByApplicationId",
      Seq("applicationId" -> appId.toString)
    )
  }

  def confirmCandidateAllocation(eventId: UniqueIdentifier, sessionId: UniqueIdentifier, candidateAllocations: CandidateAllocations)(
    implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[CandidateAllocations, HttpResponse](
      s"$apiBaseUrl/candidate-allocations/confirm-allocation/events/$eventId/sessions/$sessionId", candidateAllocations
    ).map {
      case resp if resp.status == OK => ()
      case resp if resp.status == CONFLICT =>
        throw new OptimisticLockException(s"Candidate allocation for event $eventId has changed.")
    }
  }

  def hasAnalysisExercise(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Boolean] = {
    http.GET[Boolean](s"$apiBaseUrl/application/hasAnalysisExercise", Seq("applicationId" -> applicationId.toString))
  }

  def findAdjustments(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[Adjustments]] = {
    http.GET[Option[Adjustments]](s"$apiBaseUrl/adjustments/$appId")
  }

  def considerForSdip(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[String, HttpResponse](s"$apiBaseUrl/application/consider-for-sdip/$applicationId", "").map(_ => ())
  }

  def continueAsSdip(userId: UniqueIdentifier, userIdToArchiveWith: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.PUT[String, HttpResponse](s"$apiBaseUrl/application/continue-as-sdip/$userId/$userIdToArchiveWith", "").map(_ => ())
  }

  def uploadAnalysisExercise(applicationId: UniqueIdentifier, contentType: String, fileContents: Array[Byte])(
    implicit hc: HeaderCarrier): Future[Unit] = {
    http.POSTBinary[HttpResponse](
      s"$apiBaseUrl/application/uploadAnalysisExercise?applicationId=$applicationId&contentType=$contentType", fileContents
    ).map {
      case resp if resp.status == OK => ()
      case resp if resp.status == CONFLICT => throw new CandidateAlreadyHasAnAnalysisExerciseException
    }
  }
}
// scalastyle:on

object ApplicationClient {
  sealed class CannotUpdateRecord extends Exception
  sealed class CannotSubmit extends Exception
  sealed class CannotSubmitOverriddenSubmissionDeadline extends Exception
  sealed class CannotMarkSignupCodeAsUsed(applicationId: String, code: String) extends Exception
  sealed class PersonalDetailsNotFound extends Exception
  sealed class AssistanceDetailsNotFound extends Exception
  sealed class ApplicationNotFound extends Exception
  sealed class CannotAddReferral extends Exception
  sealed class CannotWithdraw extends Exception
  sealed class OnlineTestNotFound extends Exception
  sealed class PdfReportNotFoundException extends Exception
  sealed class SiftAnswersNotFound extends Exception
  sealed class SiftExpired extends Exception
  sealed class SchemeSpecificAnswerNotFound extends Exception
  sealed class SiftAnswersIncomplete extends Exception
  sealed class SiftAnswersSubmitted extends Exception
  sealed class SiftTestNotFound(m: String) extends Exception(m)
  sealed class TestForTokenExpiredException extends Exception
  sealed class CandidateAlreadyHasAnAnalysisExerciseException extends Exception
  sealed class OptimisticLockException(m: String) extends Exception(m)
  sealed class FsacExerciseAveragesNotFound(m: String) extends Exception(m)
}
