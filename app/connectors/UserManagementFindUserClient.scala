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

import config.{FrontendAppConfig, UserManagementUrl}
import connectors.UserManagementClient.*
import connectors.exchange.{FindByUserIdRequest, UserResponse}
import models.UniqueIdentifier
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// This has been split off from the UserManagementClient class after moving to Scala 3
// in order to fix the circular dependency compile error
@Singleton
class UserManagementFindUserClient @Inject()(config: FrontendAppConfig, http: HttpClientV2)(implicit val ec: ExecutionContext) {

  private lazy val ServiceName = config.authConfig.serviceName
  private val urlPrefix = "faststream"

  val url: UserManagementUrl = config.userManagementConfig.url

  def findByUserId(userId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[UserResponse] = {
    http.post(url"${url.host}/$urlPrefix/service/$ServiceName/findUserById")
      .withBody(Json.toJson(FindByUserIdRequest(userId)))
      .execute[UserResponse]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND =>
          throw new InvalidCredentialsException(s"UserId = $userId")
      }
  }
}
