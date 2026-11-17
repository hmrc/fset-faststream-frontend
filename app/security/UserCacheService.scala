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

import com.google.inject.Inject
import play.silhouette.api.LoginInfo
import connectors.ApplicationClient.ApplicationNotFound
import connectors.exchange.*
import connectors.{ApplicationClient, UserManagementFindUserClient}

import javax.inject.Singleton
import models.{ApplicationData, CachedData, SecurityUser, UniqueIdentifier}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserCacheService @Inject()(
  applicationClient: ApplicationClient,
  userManagementClient: UserManagementFindUserClient)(
  implicit ec: ExecutionContext) extends UserService {

  override def retrieve(loginInfo: LoginInfo): Future[Option[SecurityUser]] =
    Future.successful(Some(SecurityUser(userID = loginInfo.providerKey)))

  override def refreshCachedUser(userId: UniqueIdentifier)(implicit hc: HeaderCarrier, request: Request[_]): Future[CachedData] = {
    def getApplicationResponseOpt(userId: UniqueIdentifier, frameworkId: String) = {
      (for {
        appData <- applicationClient.findApplication(userId, FrameworkId)
      } yield Some(appData)).recover {
        case _: ApplicationNotFound => None
      }
    }

    for {
      userData <- userManagementClient.findByUserId(userId)
      appDataOpt <- getApplicationResponseOpt(userId, FrameworkId)
    } yield {
      CachedData(userData.toCached, appDataOpt.map(ad => ApplicationData.fromAppRespToAppData(ad)))
    }
  }
}
