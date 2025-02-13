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

import models.SecurityUserExamples.{ActiveCandidate, SdipApplicationPersonalDetailsEntered}
import models._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

// This class supports unit testing and creates a testable version of Secure Actions that run authorization
// checks based on the roles that are defined on the controller actions.
// This trait creates a logged in Sdip user who is in a state where location preferences can be chosen.
trait TestableSecureActionsWithAuthorizationForSdipCandidate extends TestableSecureActionsWithAuthorization {
  self: FrontendController =>

  override def candidate: CachedData = ActiveCandidate

  override def candidateWithApp: CachedDataWithApp = CachedDataWithApp(ActiveCandidate.user,
    SdipApplicationPersonalDetailsEntered.copy(userId = ActiveCandidate.user.userID))
}
