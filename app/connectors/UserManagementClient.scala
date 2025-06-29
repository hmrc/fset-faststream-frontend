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
import connectors.exchange.*
import models.UniqueIdentifier
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserManagementClient @Inject()(config: FrontendAppConfig, http: HttpClientV2)(implicit val ec: ExecutionContext) {

  private val role = "candidate" // We have only one role for this application
  private lazy val ServiceName = config.authConfig.serviceName
  private val urlPrefix = "faststream"

  val url: UserManagementUrl = config.userManagementConfig.url

  def register(email: String, password: String, firstName: String, lastName: String)(implicit hc: HeaderCarrier): Future[UserResponse] = {
    http.post(url"${url.host}/$urlPrefix/add")
      .withBody(Json.toJson(AddUserRequest(email.toLowerCase, password, firstName, lastName, List(role), ServiceName)))
      .execute[HttpResponse]
      .map {
        case resp if resp.status == OK => resp.json.as[UserResponse]
        case resp if resp.status == CONFLICT => throw new EmailTakenException
      }
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == CONFLICT => throw new EmailTakenException
      }
  }

  def signIn(email: String, password: String)(implicit hc: HeaderCarrier): Future[UserResponse] = {
    http.post(url"${url.host}/$urlPrefix/authenticate")
      .withBody(Json.toJson(SignInRequest(email.toLowerCase, password, ServiceName)))
      .execute[HttpResponse]
      .map {
        case resp if resp.status == OK =>
          val response = resp.json.as[UserResponse]
          if (response.roles.head != role) throw new InvalidRoleException() else { response }
        case resp if resp.status == UNAUTHORIZED => throw new InvalidCredentialsException
      }
  }

  def activate(email: String, token: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.post(url"${url.host}/activate")
      .withBody(Json.toJson(ActivateEmailRequest(email.toLowerCase, token, ServiceName)))
      .execute[HttpResponse]
      .map {
        case resp if resp.status == OK => (): Unit
        case resp if resp.status == GONE => throw new TokenExpiredException
        case resp if resp.status == NOT_FOUND => throw new TokenEmailPairInvalidException
      }
  }

  def resendActivationCode(email: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.post(url"${url.host}/resend-activation-code")
      .withBody(Json.toJson(ResendActivationTokenRequest(email.toLowerCase, ServiceName)))
      .execute[HttpResponse]
      .map {
        case resp if resp.status == OK => (): Unit
        case resp if resp.status == NOT_FOUND => throw new InvalidEmailException
      }
  }

  def sendResetPwdCode(email: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.post(url"${url.host}/$urlPrefix/send-reset-password-code")
      .withBody(Json.toJson(SendPasswordCodeRequest(email.toLowerCase, ServiceName)))
      .execute[HttpResponse]
      .map {
        case resp if resp.status == OK => (): Unit
        case resp if resp.status == NOT_FOUND => throw new InvalidEmailException
      }
  }

  def resetPasswd(email: String, token: String, newPassword: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.post(url"${url.host}/$urlPrefix/reset-password")
      .withBody(Json.toJson(ResetPasswordRequest(email.toLowerCase, token, newPassword, ServiceName)))
      .execute[HttpResponse]
      .map {
        case resp if resp.status == OK => (): Unit
        case resp if resp.status == GONE => throw new TokenExpiredException
        case resp if resp.status == NOT_FOUND => throw new TokenEmailPairInvalidException
      }
  }

  def updateDetails(userId: UniqueIdentifier, firstName: String, lastName: String, preferredName: Option[String])(
    implicit hc: HeaderCarrier): Future[Unit] =
    http.put(url"${url.host}/$urlPrefix/service/$ServiceName/details/$userId")
      .withBody(Json.toJson(UpdateDetails(firstName, lastName, preferredName, ServiceName)))
      .execute[HttpResponse]
      .map(_ => ())

  def failedLogin(email: String)(implicit hc: HeaderCarrier): Future[UserResponse] = {
    http.put(url"${url.host}/$urlPrefix/failedAttempt")
      .withBody(Json.toJson(EmailWrapper(email.toLowerCase, ServiceName)))
      .execute[UserResponse]
      .recover {
        case eNotFound: UpstreamErrorResponse if UpstreamErrorResponse.WithStatusCode.unapply(eNotFound).contains(NOT_FOUND) =>
          throw new InvalidCredentialsException
        case eLocked: UpstreamErrorResponse if UpstreamErrorResponse.WithStatusCode.unapply(eLocked).contains(LOCKED) =>
          throw new AccountLockedOutException
    }
  }

  def find(email: String)(implicit hc: HeaderCarrier): Future[UserResponse] = {
    http.post(url"${url.host}/$urlPrefix/find")
      .withBody(Json.toJson(EmailWrapper(email.toLowerCase, ServiceName)))
      .execute[UserResponse]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new InvalidCredentialsException
    }
  }
}

object UserManagementClient {
  sealed class InvalidRoleException extends Exception
  sealed class InvalidEmailException extends Exception
  sealed class EmailTakenException extends Exception
  sealed class InvalidCredentialsException(message: String = "") extends Exception(message)
  sealed class AccountLockedOutException extends Exception
  sealed class TokenExpiredException extends Exception
}
