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

import java.util.UUID
import _root_.helpers.{NotificationType, NotificationTypeHelper}
import play.silhouette.api.{EventBus, LoginInfo}
import play.silhouette.impl.authenticators.{SessionAuthenticator, SessionAuthenticatorService}
import config.{FrontendAppConfig, SecurityEnvironment, TrackingConsentConfig}
import connectors.*
import models.ApplicationRoute.ApplicationRoute as _
import models.SecurityUserExamples.*
import models.*
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.*
import play.api.test.{CSRFTokenHelper, FakeRequest}
import play.api.test.Helpers.*
import security.{CsrCredentialsProvider, SignInService, SilhouetteComponent, UserCacheService}
import services.Phase3FeedbackService
import testkit.BaseSpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

import java.time.ZonedDateTime
import scala.concurrent.Future

abstract class BaseControllerSpec extends BaseSpec {
  implicit val rh: RequestHeader = FakeRequest("GET", "some/path")

  def currentCandidate: CachedData = ActiveCandidate

  def currentUserId = currentCandidate.user.userID

  def currentEmail = currentCandidate.user.email

  def currentUser = currentCandidate.user

  def currentCandidateWithApp: CachedDataWithApp = CachedDataWithApp(ActiveCandidate.user,
    CreatedApplication.copy(userId = ActiveCandidate.user.userID))

  def currentCandidateWithEdipApp: CachedDataWithApp = CachedDataWithApp(ActiveCandidate.user,
    CreatedApplication.copy(userId = ActiveCandidate.user.userID, applicationRoute = ApplicationRoute.Edip))

  def currentCandidateWithSdipApp: CachedDataWithApp = CachedDataWithApp(ActiveCandidate.user,
    CreatedApplication.copy(userId = ActiveCandidate.user.userID, applicationRoute = ApplicationRoute.Sdip))

  def currentApplicationId = currentCandidateWithApp.application.applicationId

  def randomUUID = UniqueIdentifier(UUID.randomUUID().toString)

  def withDefaultHeaders[T](r: FakeRequest[T]): FakeRequest[T] = {
    new FakeRequest(CSRFTokenHelper.addCSRFToken(r))
  }

  def fakeRequest = withDefaultHeaders(FakeRequest())

  val defaultApplicationRouteState = new ApplicationRouteState {
    val newAccountsStarted = true
    val newAccountsEnabled = true
    val applicationsSubmitEnabled = true
    val applicationsStartDate = Some(java.time.LocalDateTime.now)
  }

  def emptyFuture = Future.successful(())

  def assertPageTitle(result: Future[Result], expectedTitle: String) = {
    status(result) mustBe OK
    val content = contentAsString(result)
    content must include(s"<title>$expectedTitle")
  }

  def assertPageRedirection(result: Future[Result], expectedUrl: String) = {
    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(expectedUrl)
  }

  trait BaseControllerTestFixture {
    // Basic Wiring
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockMessagesApi: MessagesApi = mock[MessagesApi]
    val stubMcc: MessagesControllerComponents = stubMessagesControllerComponents(messagesApi = mockMessagesApi)
    val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
    when(mockConfig.trackingConsentConfig).thenReturn(TrackingConsentConfig(
      platformHost = None, trackingConsentHost = Some("http://localhost"), trackingConsentPath = Some("/testPath.js"), gtmContainer = None
    ))
    val mockSilhouetteComponent: SilhouetteComponent = mock[SilhouetteComponent]
    val mockApplication: Application = mock[Application]

    // Security env
    val mockEventBus: EventBus = mock[EventBus]
    val mockCredentialsProvider: CsrCredentialsProvider = mock[CsrCredentialsProvider]
    val mockAuthenticatorService: SessionAuthenticatorService = mock[SessionAuthenticatorService]
    when(mockAuthenticatorService.embed(any[Session], any[RequestHeader])).thenReturn(FakeRequest())
    val sessionAuthenticator: SessionAuthenticator = SessionAuthenticator(
      LoginInfo("fakeProvider", "fakeKey"),
      ZonedDateTime.now(),
      ZonedDateTime.now().plusDays(1),
      idleTimeout = None, fingerprint = None
    )

    when(mockAuthenticatorService.retrieve(any())).thenReturn(Future.successful(Some(sessionAuthenticator)))

    val mockUserCacheService: UserCacheService = mock[UserCacheService]
    val mockUserService: UserCacheService = mock[UserCacheService]

    val mockSecurityEnv: SecurityEnvironment = mock[config.SecurityEnvironment]
    when(mockSecurityEnv.eventBus).thenReturn(mockEventBus)
    when(mockSecurityEnv.credentialsProvider).thenReturn(mockCredentialsProvider)
    when(mockSecurityEnv.authenticatorService).thenReturn(mockAuthenticatorService)
    when(mockSecurityEnv.userCacheService).thenReturn(mockUserCacheService)
    when(mockSecurityEnv.userService).thenReturn(mockUserService)

    // MessagesApi and Messages
    implicit val mockMessages: Messages = mock[Messages]
    when(mockMessages.messages).thenReturn(mockMessages)
    when(mockMessages.apply(anyString(), any())).thenAnswer(new Answer[String]() {
      override def answer(invocationOnMock: InvocationOnMock): String = {
        invocationOnMock.getArgument(0, classOf[String])
      }
    })
    when(mockMessages.apply(any[Seq[String]], any())).thenAnswer(new Answer[String]() {
      override def answer(invocationOnMock: InvocationOnMock): String = {
        invocationOnMock.getArgument(0, classOf[String])
      }
    })
    when(mockMessagesApi.preferred(any[RequestHeader])).thenReturn(mockMessages)
    when(mockMessagesApi.apply(anyString(), any())(any())).thenAnswer(new Answer[String]() {
      override def answer(invocationOnMock: InvocationOnMock): String = {
        invocationOnMock.getArgument(0, classOf[String])
      }
    })
    when(mockMessagesApi.apply(any[Seq[String]], any())(any())).thenAnswer(new Answer[String]() {
      override def answer(invocationOnMock: InvocationOnMock): String = {
        invocationOnMock.getArgument(0, classOf[String])
      }
    })

    // Clients
    val mockUserManagementClient = mock[UserManagementClient]
    val mockSchemeClient  = mock[SchemeClient]
    val mockSdipLocationsClient  = mock[SdipLocationsClient]
    val mockReferenceDataClient = mock[ReferenceDataClient]
    val mockApplicationClient = mock[ApplicationClient]
    val mockAssessmentScoresClient = mock[AssessmentScoresClient]
    val mockSiftClient = mock[SiftClient]

    // Services
    val mockSignInService = mock[SignInService]
    val mockPhase3FeedbackService = mock[Phase3FeedbackService]

    // NotificationType
    val mockNotificationTypeHelper = mock[NotificationTypeHelper]
    when(mockNotificationTypeHelper.success(anyString(), any())(any())).thenAnswer(new Answer[(NotificationType, String)]() {
      override def answer(invocationOnMock: InvocationOnMock): (NotificationType, String) = {
        NotificationType.Success -> invocationOnMock.getArgument(0, classOf[String])
      }
    })
    when(mockNotificationTypeHelper.warning(anyString(), any())(any())).thenAnswer(new Answer[(NotificationType, String)]() {
      override def answer(invocationOnMock: InvocationOnMock): (NotificationType, String) = {
        NotificationType.Warning -> invocationOnMock.getArgument(0, classOf[String])
      }
    })
    when(mockNotificationTypeHelper.info(anyString(), any())(any())).thenAnswer(new Answer[(NotificationType, String)]() {
      override def answer(invocationOnMock: InvocationOnMock): (NotificationType, String) = {
        NotificationType.Info -> invocationOnMock.getArgument(0, classOf[String])
      }
    })
    when(mockNotificationTypeHelper.danger(anyString(), any())(any())).thenAnswer(new Answer[(NotificationType, String)]() {
      override def answer(invocationOnMock: InvocationOnMock): (NotificationType, String) = {
        NotificationType.Danger -> invocationOnMock.getArgument(0, classOf[String])
      }
    })

    val anyContentMock = mock[AnyContent]
  }
}
