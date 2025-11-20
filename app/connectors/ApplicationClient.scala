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

import config.FrontendAppConfig
import connectors.TokenEmailPairInvalidException
import connectors.exchange.*
import connectors.exchange.GeneralDetails.*
import connectors.exchange.Questionnaire.*
import connectors.exchange.OnboardQuestions
import connectors.exchange.campaignmanagement.AfterDeadlineSignupCodeUnused
import connectors.exchange.candidateevents.{CandidateAllocationWithEvent, CandidateAllocations}
import connectors.exchange.candidatescores.ExerciseAverageResult
import connectors.exchange.sift.SiftState
import models.{Adjustments, ApplicationRoute, UniqueIdentifier}
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// scalastyle:off number.of.methods
@Singleton
class ApplicationClient @Inject() (config: FrontendAppConfig, http: HttpClientV2)(implicit ec: ExecutionContext)
  extends TestDataGeneratorClient(config, http) { //TODO: why does this class extend TestDataGeneratorClient - fix!!

  import ApplicationClient.*

  val apiBaseUrl: String = config.faststreamBackendConfig.url.host + config.faststreamBackendConfig.url.base

  def afterDeadlineSignupCodeUnusedAndValid(afterDeadlineSignupCode: String)(implicit hc: HeaderCarrier)
  : Future[AfterDeadlineSignupCodeUnused] = {
    http.get(url"$apiBaseUrl/campaign-management/afterDeadlineSignupCodeUnusedAndValid?code=$afterDeadlineSignupCode")
      .execute[AfterDeadlineSignupCodeUnused]
  }

  def createApplication(userId: UniqueIdentifier, frameworkId: String,
    applicationRoute: ApplicationRoute.ApplicationRoute = ApplicationRoute.Faststream)
    (implicit hc: HeaderCarrier): Future[ApplicationResponse] = {
    http.put(url"$apiBaseUrl/application/create")
      .withBody(Json.toJson(CreateApplicationRequest(userId, frameworkId, applicationRoute)))
      .execute[ApplicationResponse]
  }

  def overrideSubmissionDeadline(applicationId: UniqueIdentifier, overrideRequest: OverrideSubmissionDeadlineRequest)(
    implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/application/overrideSubmissionDeadline/$applicationId")
      .withBody(Json.toJson(overrideRequest))
      .execute[HttpResponse]
      .map { response =>
        if (response.status != OK) { throw new CannotSubmitOverriddenSubmissionDeadline } else { () }
      }
  }

  def markSignupCodeAsUsed(code: String, applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.get(url"$apiBaseUrl/application/markSignupCodeAsUsed?code=$code&applicationId=${applicationId.toString}")
      .execute[HttpResponse]
      .map { response =>
        if (response.status != OK) {
          throw new CannotMarkSignupCodeAsUsed(applicationId.toString, code)
        } else {
          ()
        }
      }
  }

  def submitApplication(userId: UniqueIdentifier, applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/application/submit/$userId/$applicationId")
      .withBody(Json.toJson(""))
      .execute[Either[UpstreamErrorResponse, Unit]]
      .map {
        case Right(_) => ()
        case Left(badRequestEx) if badRequestEx.statusCode == BAD_REQUEST => throw new CannotSubmit
        case Left(ex) => throw ex
      }
  }

  def withdrawApplication(applicationId: UniqueIdentifier, reason: WithdrawApplication)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/application/withdraw/$applicationId")
      .withBody(Json.toJson(reason))
      .execute[Either[UpstreamErrorResponse, Unit]]
      .map {
        case Right(_) => ()
        case Left(notFoundEx) if notFoundEx.statusCode == NOT_FOUND => throw new CannotWithdraw()
        case Left(ex) => throw ex
      }
  }

  def withdrawScheme(applicationId: UniqueIdentifier, withdrawal: WithdrawScheme)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/application/$applicationId/scheme/withdraw")
      .withBody(Json.toJson(withdrawal))
      .execute[Either[UpstreamErrorResponse, Unit]]
      .map {
        case Right(_) => ()
        case Left(forbiddenEx) if forbiddenEx.statusCode == FORBIDDEN => throw new SiftExpired
        case Left(_) => throw new CannotWithdraw
      }
  }

  def addReferral(userId: UniqueIdentifier, referral: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/media/create")
      .withBody(Json.toJson(AddReferral(userId, referral)))
      .execute[HttpResponse]
      .map {
        case response if response.status == CREATED => ()
        case response if response.status == BAD_REQUEST => throw new CannotAddReferral
      }
  }

  // TODO: looks like this is only called by test classes
  def getApplicationProgress(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[ProgressResponse] = {
    http.get(url"$apiBaseUrl/application/progress/$applicationId")
      .execute[ProgressResponse]
  }

  def findApplication(userId: UniqueIdentifier, frameworkId: String)(implicit hc: HeaderCarrier): Future[ApplicationResponse] = {
    http.get(url"$apiBaseUrl/application/find/user/$userId/framework/$frameworkId")
      .execute[ApplicationResponse]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new ApplicationNotFound
      }
  }

  def findFsacExerciseAverages(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[ExerciseAverageResult] = {
    http.get(url"$apiBaseUrl/application/$applicationId/fsacExerciseAverages")
      .execute[ExerciseAverageResult]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND =>
          val msg = s"Found no fsac exercise averages for application id: $applicationId"
          throw new FsacExerciseAveragesNotFound(msg)
      }
  }

  def updatePersonalDetails(applicationId: UniqueIdentifier, userId: UniqueIdentifier, personalDetails: GeneralDetails)
                           (implicit hc: HeaderCarrier): Future[Unit] = {
    http.post(url"$apiBaseUrl/personal-details/$userId/$applicationId")
      .withBody(Json.toJson(personalDetails))
      .execute[HttpResponse]
      .map {
        case resp if resp.status == CREATED => ()
        case resp if resp.status == BAD_REQUEST => throw new CannotUpdateRecord
      }
  }

  def getPersonalDetails(userId: UniqueIdentifier, applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[GeneralDetails] = {
    http.get(url"$apiBaseUrl/personal-details/$userId/$applicationId")
      .execute[GeneralDetails]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new PersonalDetailsNotFound
      }
  }

  def updateAssistanceDetails(applicationId: UniqueIdentifier, userId: UniqueIdentifier, assistanceDetails: AssistanceDetails)
                             (implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/assistance-details/$userId/$applicationId")
      .withBody(Json.toJson(assistanceDetails))
      .execute[HttpResponse]
      .map {
        case resp if resp.status == CREATED => ()
        case resp if resp.status == BAD_REQUEST => throw new CannotUpdateRecord
      }
  }

  def getAssistanceDetails(userId: UniqueIdentifier, applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[AssistanceDetails] = {
    http.get(url"$apiBaseUrl/assistance-details/$userId/$applicationId")
      .execute[AssistanceDetails]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new AssistanceDetailsNotFound
      }
  }

  def updateQuestionnaire(applicationId: UniqueIdentifier, sectionId: String, questionnaire: Questionnaire)
                         (implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/questionnaire/$applicationId/$sectionId")
      .withBody(Json.toJson(questionnaire))
      .execute[HttpResponse]
      .map {
        case resp if resp.status == ACCEPTED => ()
        case resp if resp.status == BAD_REQUEST => throw new CannotUpdateRecord
      }
  }

  def updatePreview(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/application/preview/$applicationId")
      .withBody(Json.toJson(PreviewRequest(true)))
      .execute[HttpResponse]
      .map {
        case resp if resp.status == OK => ()
        case resp if resp.status == BAD_REQUEST => throw new CannotUpdateRecord
      }
  }

  def verifyInvigilatedToken(email: String, token: String)(implicit hc: HeaderCarrier): Future[InvigilatedTestUrl] = {
    http.post(url"$apiBaseUrl/online-test/phase2/verifyAccessCode")
      .withBody(Json.toJson(VerifyInvigilatedTokenUrlRequest(email.toLowerCase, token)))
      .execute[InvigilatedTestUrl]
      .recover {
        case notFoundEx: UpstreamErrorResponse if notFoundEx.statusCode == NOT_FOUND => throw new TokenEmailPairInvalidException
        case forbiddenEx: UpstreamErrorResponse if forbiddenEx.statusCode == FORBIDDEN => throw new TestForTokenExpiredException
      }
  }

  // psi code start
  def getPhase1Tests(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Seq[PsiTest]] = {
    http.get(url"$apiBaseUrl/phase1-tests/$appId")
      .execute[Seq[PsiTest]]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new OnlineTestNotFound
      }
  }

  def getPhase1TestProfile(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Phase1TestGroupWithNames] = {
    http.get(url"$apiBaseUrl/online-test/psi/phase1/candidate/$appId")
      .execute[Phase1TestGroupWithNames]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new OnlineTestNotFound
      }
  }

  def getPhase2TestProfile(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Phase2TestGroupWithActiveTest] = {
    http.get(url"$apiBaseUrl/online-test/phase2/candidate/$appId")
      .execute[Phase2TestGroupWithActiveTest]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new OnlineTestNotFound
      }
  }

  def getPhase1TestGroupWithNamesByOrderId(orderId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Phase1TestGroupWithNames] = {
    http.get(url"$apiBaseUrl/online-test/phase1/candidate/orderId/$orderId")
      .execute[Phase1TestGroupWithNames]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new OnlineTestNotFound
      }
  }

  def getPhase2TestProfileByOrderId(orderId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Phase2TestGroupWithActiveTest] = {
    http.get(url"$apiBaseUrl/online-test/phase2/candidate/orderId/$orderId")
      .execute[Phase2TestGroupWithActiveTest]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new OnlineTestNotFound
      }
  }

  def completeTestByOrderId(orderId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/psi/$orderId/complete")
      .withBody(Json.toJson(""))
      .execute[HttpResponse]
      .map(_ => ())
  }
  // psi code end

  def getPhase3TestGroup(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Phase3TestGroup] = {
    http.get(url"$apiBaseUrl/phase3-test-group/$appId")
      .execute[Phase3TestGroup]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new OnlineTestNotFound
      }
  }

  def getPhase3Results(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[List[SchemeEvaluationResult]]] = {
    http.get(url"$apiBaseUrl/application/$appId/phase3/results")
      .execute[Option[List[SchemeEvaluationResult]]]
  }

  def getSiftTestGroup(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[SiftTestGroupWithActiveTest] = {
    http.get(url"$apiBaseUrl/psi/sift-test-group/$appId")
      .execute[SiftTestGroupWithActiveTest]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new SiftTestNotFound(s"No sift test group found for $appId")
      }
  }

  def getSiftState(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[SiftState]] = {
    http.get(url"$apiBaseUrl/sift-candidate/state/$appId")
      .execute[Option[SiftState]]
  }

  def getSiftResults(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[List[SchemeEvaluationResult]]] = {
    http.get(url"$apiBaseUrl/application/$appId/sift/results")
      .execute[Option[List[SchemeEvaluationResult]]]
  }

  def getCurrentSchemeStatus(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Seq[SchemeEvaluationResultWithFailureDetails]] = {
    http.get(url"$apiBaseUrl/application/$appId/currentSchemeStatus")
      .execute[Seq[SchemeEvaluationResultWithFailureDetails]]
  }

  private def encodeUrlParam(str: String) = URLEncoder.encode(str, "UTF-8")

  def startPhase3TestByToken(launchpadInviteId: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/launchpad/${encodeUrlParam(launchpadInviteId)}/markAsStarted")
      .withBody(Json.toJson(""))
      .execute[HttpResponse]
      .map(_ => ())
  }

  def completePhase3TestByToken(launchpadInviteId: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/launchpad/${encodeUrlParam(launchpadInviteId)}/markAsComplete")
      .withBody(Json.toJson(""))
      .execute[HttpResponse]
      .map(_ => ())
  }

  def startTest(orderId: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/psi/$orderId/start")
      .withBody(Json.toJson(""))
      .execute[HttpResponse]
      .map(_ => ())
  }

  def startSiftTest(orderId: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/psi/sift-test/$orderId/start")
      .withBody(Json.toJson(""))
      .execute[HttpResponse]
      .map(_ => ())
  }

  def confirmAllocation(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.post(url"$apiBaseUrl/allocation-status/confirm/$appId")
      .withBody(Json.toJson(""))
      .execute[HttpResponse]
      .map(_ => ())
  }

  def candidateAllocationEventWithSession(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[List[CandidateAllocationWithEvent]] = {
    http.get(url"$apiBaseUrl/candidate-allocations/sessions/findByApplicationId?applicationId=${appId.toString}")
      .execute[List[CandidateAllocationWithEvent]]
  }

  def confirmCandidateAllocation(eventId: UniqueIdentifier, sessionId: UniqueIdentifier, candidateAllocations: CandidateAllocations)(
    implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/candidate-allocations/confirm-allocation/events/$eventId/sessions/$sessionId")
      .withBody(Json.toJson(candidateAllocations))
      .execute[HttpResponse]
      .map {
        case resp if resp.status == OK => ()
        case resp if resp.status == CONFLICT =>
          throw new OptimisticLockException(s"Candidate allocation for event $eventId has changed.")
      }
  }

  def hasAnalysisExercise(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Boolean] = {
    http.get(url"$apiBaseUrl/application/hasAnalysisExercise?applicationId=${applicationId.toString}")
      .execute[Boolean]
  }

  def findAdjustments(appId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[Adjustments]] = {
    http.get(url"$apiBaseUrl/adjustments/$appId")
      .execute[Option[Adjustments]]
  }

  def considerForSdip(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/application/consider-for-sdip/$applicationId")
      .withBody(Json.toJson(""))
      .execute[HttpResponse]
      .map(_ => ())
  }

  def continueAsSdip(userId: UniqueIdentifier, userIdToArchiveWith: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBaseUrl/application/continue-as-sdip/$userId/$userIdToArchiveWith")
      .withBody(Json.toJson(""))
      .execute[HttpResponse]
      .map(_ => ())
  }

  // TODO: this functionality is no longer available via the candidate ui. It is only available via the admin ui
  // so no need to spend time fixing this
  def uploadAnalysisExercise(applicationId: UniqueIdentifier, contentType: String, fileContents: Array[Byte])(
    implicit hc: HeaderCarrier): Future[Unit] = {
    scala.concurrent.Future.successful(())
/*
    http.POSTBinary[HttpResponse](
      s"$apiBaseUrl/application/uploadAnalysisExercise?applicationId=$applicationId&contentType=$contentType", fileContents
    ).map {
      case resp if resp.status == OK => ()
      case resp if resp.status == CONFLICT => throw new CandidateAlreadyHasAnAnalysisExerciseException
    }
 */
  }
}
// scalastyle:on

object ApplicationClient {
  sealed class CannotUpdateRecord extends Exception
  sealed class CannotUpdateRecord2(m: String) extends Exception(m)
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
