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

package testkit

import models.SecurityUserExamples.{ActiveCandidate, CreatedApplication}
import models._
import play.api.Logging
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.silhouette.api.LoginInfo
import play.silhouette.api.actions.{SecuredRequest, UserAwareRequest}
import play.silhouette.impl.authenticators.SessionAuthenticator
import security.Roles.CsrAuthorization
import security.{SecureActions, SecurityEnvironment}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.ZonedDateTime
import java.util.UUID
import scala.concurrent.Future

// This class supports unit testing and creates a testable version of Secure Actions
// using a Faststream candidate as the logged in user
trait TestableSecureActions extends SecureActions with Logging {
  self: FrontendController =>

  def candidate: CachedData = ActiveCandidate
  def candidateWithApp: CachedDataWithApp = CachedDataWithApp(ActiveCandidate.user,
    CreatedApplication.copy(userId = ActiveCandidate.user.userID))

  // scalastyle:off method.name
  override def CSRSecureAction(role: CsrAuthorization)(block: SecuredRequest[_,_] => CachedData => Future[Result]): Action[AnyContent] =
    execute(candidate)(role)(block)

  override def CSRSecureAppAction(role: CsrAuthorization)(
    block: SecuredRequest[_,_] => CachedDataWithApp => Future[Result]): Action[AnyContent] =
    execute(candidateWithApp)(role)(block)

  override def CSRUserAwareAction(block: UserAwareRequest[_,_] => Option[CachedData] => Future[Result]): Action[AnyContent] =
    Action.async { request =>
      val userAwareRequest = UserAwareRequest(identity = None, authenticator = None, request)
      block(userAwareRequest)(None)
    }
    // scalastyle:on

  private def execute[T](result: T)(role: CsrAuthorization)(block: SecuredRequest[_,_] => T => Future[Result]): Action[AnyContent] =
    Action.async { request =>
      val securedRequest = buildSecuredRequest(request)
      block(securedRequest)(result)
    }

  private def buildSecuredRequest(request: Request[AnyContent]) =
    SecuredRequest[SecurityEnvironment, AnyContent](
      SecurityUser(UUID.randomUUID.toString),
      SessionAuthenticator(
        LoginInfo("fakeProvider", "fakeKey"),
        now,
        now.plusDays(1),
        idleTimeout = None, fingerprint = None
      ),
      request
    )

  private def now = ZonedDateTime.now()
}

// scalastyle:off method.name
trait TestableCSRSecureAction extends SecureActions {
  self: FrontendController =>
  /**
   * Wraps the csrAction helper on a secure action.
   * If the user is not logged in then the onNotAuthenticated method on global (or controller if it overrides it) will be called.
   * The Action gets a default role that checks  if the user is active or not. \
   * If the user is inactive then the onNotAuthorized method on global will be called.
   */
  override def CSRSecureAction(role: CsrAuthorization)(block: SecuredRequest[_,_] => CachedData => Future[Result]): Action[AnyContent] = {
    Action.async { request =>
      val secReq = SecuredRequest[SecurityEnvironment, AnyContent](
        SecurityUser(UniqueIdentifier(UUID.randomUUID.toString).toString()),
        SessionAuthenticator(
          LoginInfo("fakeProvider", "fakeKey"),
          now,
          now.plusDays(1),
          idleTimeout = None, fingerprint = None
        ), request
      )
      block(secReq)(CachedDataExample.ActiveCandidate)
    }
  }

  private def now = ZonedDateTime.now()
}

trait TestableCSRUserAwareAction extends SecureActions {
  self: FrontendController =>

  override def CSRUserAwareAction(block: UserAwareRequest[_,_] => Option[CachedData] => Future[Result]): Action[AnyContent] =
    Action.async { request =>
      val userAwareReq = UserAwareRequest[SecurityEnvironment, AnyContent](
        Some(SecurityUser("userId")),
        Some(SessionAuthenticator(
          LoginInfo("fakeProvider", "fakeKey"),
          ZonedDateTime.now(),
          ZonedDateTime.now().plusDays(1),
          idleTimeout = None, fingerprint = None
        )), request)

      val session = SessionKeys.sessionId -> s"session-${UUID.randomUUID}"

      block(userAwareReq)(Some(CachedDataExample.ActiveCandidate))
        .map(_.addingToSession(session)(request))
    }
}

trait NoIdentityTestableCSRUserAwareAction extends SecureActions {
  self: FrontendController =>

  override def CSRUserAwareAction(block: UserAwareRequest[_,_] => Option[CachedData] => Future[Result]): Action[AnyContent] =
    Action.async { request =>
      val userAwareReq = UserAwareRequest(identity = None, authenticator = None, request)

      val sessionId = SessionKeys.sessionId -> s"session-${UUID.randomUUID}"

      block(userAwareReq)(Some(CachedDataExample.ActiveCandidate)).map { value =>
        value.addingToSession(sessionId)(request)
      }
    }
}
// scalastyle:on method.name
