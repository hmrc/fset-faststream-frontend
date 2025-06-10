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

import com.github.tomakehurst.wiremock.client.WireMock.*
import config.*
import connectors.UserManagementClient.*
import connectors.exchange.*
import models.UniqueIdentifier
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.client.HttpClientV2

class UserManagementFindUserClientWithWireMockSpec extends BaseConnectorWithWireMockSpec {

  val urlPrefix = "faststream"
  val serviceName = "testServiceName"
  val userId: UniqueIdentifier = UniqueIdentifier("5efd6e38-be9e-4007-a15f-44acc4d75ec5")

  "findByUserId" should {
    val endpoint = s"/$urlPrefix/service/$serviceName/findUserById"

    val findByUserIdRequest = FindByUserIdRequest(userId)

    "handle a response indicating success" in new TestFixture {
      val response = UserResponse(
        "firstName",
        "lastName",
        preferredName = None,
        isActive = true,
        userId,
        "joeblogs@test.com",
        disabled = false,
        lockStatus = "",
        roles = List("candidate"),
        serviceName,
        phoneNumber = None
      )

      stubFor(post(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalTo(Json.toJson(findByUserIdRequest).toString))
        .willReturn(aResponse().withStatus(OK).withBody(Json.toJson(response).toString))
      )

      val result = client.findByUserId(userId).futureValue
      result mustBe response
    }

    "handle a response indicating not found" in new TestFixture {
      stubFor(post(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalTo(Json.toJson(findByUserIdRequest).toString))
        .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      val result = client.findByUserId(userId).failed.futureValue
      result mustBe an[InvalidCredentialsException]
    }
  }

  trait TestFixture extends BaseConnectorTestFixture {
    val mockConfig = new FrontendAppConfig(mockConfiguration, mockEnvironment) {
      override lazy val userManagementConfig = UserManagementConfig(UserManagementUrl(s"http://localhost:$wireMockPort"))
      override lazy val authConfig = AuthConfig(serviceName)
    }
    val ws = app.injector.instanceOf(classOf[WSClient])
    val http = app.injector.instanceOf(classOf[HttpClientV2])
    val client = new UserManagementFindUserClient(mockConfig, http)
  }
}
