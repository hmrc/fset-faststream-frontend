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

import controllers.routes
import models._
import play.api.Logging
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.silhouette.api.LoginInfo
import play.silhouette.api.actions.{SecuredRequest, UserAwareRequest}
import play.silhouette.impl.authenticators.SessionAuthenticator
import security.Roles.CsrAuthorization
import security.{SecureActions, SecurityEnvironment}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.ZonedDateTime
import java.util.UUID
import scala.concurrent.Future

// This class supports unit testing and creates a testable version of Secure Actions that run authorization
// checks based on the roles that are defined on the controller actions.
// This can be extended with either a Faststream or a Sdip candidate.
trait TestableSecureActionsWithAuthorization extends SecureActions with Logging {
  self: FrontendController =>

  def candidate: CachedData
  def candidateWithApp: CachedDataWithApp

  // scalastyle:off method.name
  override def CSRSecureAction(role: CsrAuthorization)(block: SecuredRequest[_,_] => CachedData => Future[Result]): Action[AnyContent] =
    execute(candidate)(role)(block)

  override def CSRSecureAppAction(role: CsrAuthorization)(
    block: SecuredRequest[_,_] => CachedDataWithApp => Future[Result]): Action[AnyContent] = {
    execute(candidateWithApp)(role)(block)
  }

  override def CSRUserAwareAction(block: UserAwareRequest[_,_] => Option[CachedData] => Future[Result]): Action[AnyContent] = {
    Action.async { request =>
      val userAwareRequest = UserAwareRequest(identity = None, authenticator = None, request)
      block(userAwareRequest)(None)
    }
  }

  private def execute[T](result: T)(role: CsrAuthorization)(block: SecuredRequest[_,_] => T => Future[Result]): Action[AnyContent] = {
    Action.async { request =>
      if (!role.isAuthorized(candidateWithApp)(request)) {
        logger.info(s"$role is not authorized for candidate in this test - calling gotoUnauthorised")
        gotoUnauthorised
      } else {
        val securedRequest = buildSecuredRequest(request)
        block(securedRequest)(result)
      }
    }
  }

  private def gotoUnauthorised =
    Future.successful(Redirect(routes.HomeController.present()).flashing("danger" -> "access.denied"))

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
