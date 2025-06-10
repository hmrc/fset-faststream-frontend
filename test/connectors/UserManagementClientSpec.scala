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

import config.{AuthConfig, FrontendAppConfig, UserManagementConfig, UserManagementUrl}
import connectors.UserManagementClient.{EmailTakenException, InvalidCredentialsException, InvalidRoleException}
import connectors.exchange.UserResponse
import models.UniqueIdentifier
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.{CONFLICT, OK, UNAUTHORIZED}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.RequestBuilder
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, UpstreamErrorResponse}

import java.net.URL
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class UserManagementClientSpec extends BaseConnectorSpec {

  val userId1 = UniqueIdentifier(UUID.randomUUID.toString)

  "register" should {
    "return a UserResponse upon success" in new TestFixture {
      val userResponse = UserResponse(
        "peter", "griffin", preferredName = None, isActive = true, userId1, "test@email.com",
        disabled = false, "UNLOCKED", List("candidate"), "faststream", phoneNumber = None
      )
      val httpResponse = HttpResponse(OK, Json.toJson(userResponse), Map.empty[String, Seq[String]])
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(httpResponse))

      val response = client.register("test@email.com", "password", "peter", "griffin").futureValue
      response mustBe userResponse
    }

    "throw EmailTakenException if the email address is already taken" in new TestFixture {
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.failed(UpstreamErrorResponse(message = "", CONFLICT)))

      val response = client.register("test@email.com", "password", "peter", "griffin").failed.futureValue
      response mustBe an[EmailTakenException]
    }
  }

  "signIn" should {
    val userResponse = UserResponse(
      "firstName", "lastName", Some("preferredName"), isActive = true, userId1, "test@email.com",
      disabled = false, lockStatus = "", roles = List("candidate"), service = "faststream", phoneNumber = None
    )

    "return a UserResponse upon success" in new TestFixture {
      val httpResponse = HttpResponse(OK, Json.toJson(userResponse), Map.empty[String, Seq[String]])
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(httpResponse))

      val response = client.signIn("test@email.com", "password").futureValue
      response mustBe userResponse
    }

    "throw InvalidRoleException if the role is not supported" in new TestFixture {
      val userWithUnsupportedRole = userResponse.copy(roles = List("unsupported-role"))
      val httpResponse = HttpResponse(OK, Json.toJson(userWithUnsupportedRole), Map.empty[String, Seq[String]])
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(httpResponse))

      val result = client.signIn(userWithUnsupportedRole.email, "password").failed.futureValue
      result mustBe an[InvalidRoleException]
    }

    "throw InvalidCredentialsException if the user is not authorized" in new TestFixture {
      val httpResponse = HttpResponse(UNAUTHORIZED, Json.toJson(userResponse), Map.empty[String, Seq[String]])
      when(requestBuilderExecute[HttpResponse]).thenReturn(Future.successful(httpResponse))

      val result = client.signIn(userResponse.email, "password").failed.futureValue
      result mustBe an[InvalidCredentialsException]
    }
  }

  trait TestFixture extends BaseConnectorTestFixture {
    val mockConfig = new FrontendAppConfig(mockConfiguration, mockEnvironment) {
      val userManagementUrl = UserManagementUrl("http://localhost")
      override lazy val userManagementConfig = UserManagementConfig(userManagementUrl)
      override lazy val authConfig = AuthConfig("faststream")
    }
    val client = new UserManagementClient(mockConfig, mockHttp)

    val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]

    when(mockHttp.post(any[URL])(any[HeaderCarrier])).thenReturn(mockRequestBuilder)

    when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)

    def requestBuilderExecute[A]: Future[A] = mockRequestBuilder.execute[A](any[HttpReads[A]], any[ExecutionContext])
  }
}
