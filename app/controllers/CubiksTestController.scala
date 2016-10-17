/*
 * Copyright 2016 HM Revenue & Customs
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

import config.CSRHttp
import connectors.ApplicationClient
import connectors.exchange.CubiksTest
import models.UniqueIdentifier
import security.Roles.{ OnlineTestInvitedRole, Phase2TestInvitedRole, Phase3TestInvitedRole }
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object CubiksTestController extends CubiksTestController(ApplicationClient) {
  val http = CSRHttp
}

abstract class CubiksTestController(applicationClient: ApplicationClient) extends BaseController(applicationClient) {

  def startPhase1Tests = CSRSecureAppAction(OnlineTestInvitedRole) { implicit request =>
    implicit cachedUserData =>
     applicationClient.getPhase1TestProfile(cachedUserData.application.applicationId).flatMap { phase1TestProfile =>
       startCubiksTest(phase1TestProfile.tests)
      }
  }

  def startPhase2Tests = CSRSecureAppAction(Phase2TestInvitedRole) { implicit request =>
    implicit cachedUserData =>
      applicationClient.getPhase2TestProfile(cachedUserData.application.applicationId).flatMap { phase2TestProfile =>
        startCubiksTest(phase2TestProfile.activeTests)
      }
  }

  // TODO: permission this endpoint
  def startPhase3Tests = CSRSecureAppAction(Phase3TestInvitedRole) { implicit request =>
    implicit cachedUserData =>
      applicationClient.getPhase3TestGroup(cachedUserData.application.applicationId).flatMap { testProfile =>
        // If we've started but not completed a test we still want to send them to that
        // test link to continue with it
        testProfile.tests.find(!_.completed).map { testToStart =>
          // applicationClient.startTest(testToStart.cubiksUserId)
          Future.successful(Redirect(testToStart.testUrl))
        }.getOrElse(Future.successful(NotFound))
      }
  }

  def completeSjqByTokenAndContinuePhase1Tests(token: UniqueIdentifier) = CSRUserAwareAction { implicit request =>
    implicit user =>
      applicationClient.completeTestByToken(token).map { _ =>
        Ok(views.html.application.onlineTests.sjqComplete_continuePhase1Tests())
      }
  }

  def completePhase1TestsByToken(token: UniqueIdentifier) = CSRUserAwareAction { implicit request =>
    implicit user =>
      applicationClient.completeTestByToken(token).map { _ =>
        Ok(views.html.application.onlineTests.phase1TestsComplete())
      }
  }

  def completePhase2TestsByToken(token: UniqueIdentifier) = CSRUserAwareAction { implicit request =>
    implicit user =>
      applicationClient.completeTestByToken(token).map { _ =>
        Redirect(routes.HomeController.present())
      }
  }

  def completeTestByToken(token: UniqueIdentifier) = CSRUserAwareAction { implicit request =>
    implicit user =>
      applicationClient.completeTestByToken(token).map { _ =>
        Ok(views.html.application.onlineTests.onlineTestSuccess())
      }
  }

  private def startCubiksTest(cubiksTests: Iterable[CubiksTest])(implicit hc: HeaderCarrier) = {
    cubiksTests.find(!_.completed).map { testToStart =>
      applicationClient.startTest(testToStart.cubiksUserId)
      Future.successful(Redirect(testToStart.testUrl))
    }.getOrElse(Future.successful(NotFound))
  }

}