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

package security

import java.util.UUID
import connectors.exchange.CivilServiceExperienceDetailsExamples.*
import connectors.exchange.ProgressExamples
import models.ApplicationData.ApplicationStatus
import models.ApplicationData.ApplicationStatus.{CREATED, *}
import models.CachedDataExample.*
import models.*
import play.api.mvc.{AnyContentAsEmpty, RequestHeader}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import security.Roles.{CsrAuthorization, WithdrawComponent}
import testkit.UnitSpec

class RolesSpec extends UnitSpec {
  import RolesSpec._

  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, "")

  "hasFastPassBeenApproved" must {
    val user = activeUser(ApplicationStatus.SUBMITTED)
    "return true if the candidate fastPass has been accepted" in {
      val appData = CreatedApplication.copy(civilServiceExperienceDetails = Some(CivilServantExperienceFastPassApproved))
      implicit val rh: RequestHeader = mock[RequestHeader]
      RoleUtils.hasFastPassBeenApproved(user.copy(application = Some(appData)))(rh) mustBe true
    }
    "return false if the candidate fastPass has not been accepted" in {
      val appData = CreatedApplication.copy(civilServiceExperienceDetails = Some(CivilServantExperienceFastPassRejected))
      implicit val rh: RequestHeader = mock[RequestHeader]
      RoleUtils.hasFastPassBeenApproved(user.copy(application = Some(appData)))(rh) mustBe false
    }
    "return false if the acceptance flag is not present" in {
      val appData = CreatedApplication.copy(civilServiceExperienceDetails = Some(CivilServantExperience))
      implicit val rh: RequestHeader = mock[RequestHeader]
      RoleUtils.hasFastPassBeenApproved(user.copy(application = Some(appData)))(rh) mustBe false
    }
    "return false if there are no civil servant details" in {
      val appData = CreatedApplication.copy(civilServiceExperienceDetails = None)
      implicit val rh: RequestHeader = mock[RequestHeader]
      RoleUtils.hasFastPassBeenApproved(user.copy(application = Some(appData)))(rh) mustBe false
    }
  }

  "Withdraw Component" must {
    "be enabled only for specific roles" in {
      val disabledStatuses = List(IN_PROGRESS, WITHDRAWN, CREATED, PHASE3_TESTS_PASSED_NOTIFIED)
      val enabledStatuses = ApplicationStatus.values.toList.diff(disabledStatuses)

      assertValidAndInvalidStatuses(WithdrawComponent, enabledStatuses, disabledStatuses)
    }
  }

  def assertValidAndInvalidStatuses(
    role: CsrAuthorization,
    valid: List[ApplicationStatus.Value], invalid: List[ApplicationStatus.Value]
  ) = {
    valid.foreach { validStatus =>
      withClue(s"$validStatus is not accepted by $role") {
        role.isAuthorized(activeUser(validStatus))(request) mustBe true
      }
    }

    invalid.foreach { invalidStatus =>
      withClue(s"$invalidStatus is accepted by $role") {
        role.isAuthorized(activeUser(invalidStatus))(request) mustBe false
      }
    }
  }
}

object RolesSpec {
  val id: UniqueIdentifier = UniqueIdentifier(UUID.randomUUID().toString)

  def activeUser(applicationStatus: ApplicationStatus, progress: Progress = ProgressExamples.FullProgress): CachedData = CachedData(CachedUser(
    userID = id,
    "John", "Biggs", preferredName = None, "aaa@bbb.com", isActive = true, "locked"
  ), application = Some(
    ApplicationData(
      applicationId = id, userId = id, applicationStatus, ApplicationRoute.Faststream, progress,
      civilServiceExperienceDetails = None, edipCompleted = None, overriddenSubmissionDeadline = None
    )
  ))

  def registeredUser(applicationStatus: ApplicationStatus): CachedData = CachedData(CachedUser(
    userID = id,
    "John", "Biggs", preferredName = None, "aaa@bbb.com", isActive = true, "locked"
  ), application = None)
}
