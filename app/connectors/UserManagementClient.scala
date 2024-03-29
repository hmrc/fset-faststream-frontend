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

import config.{CSRHttp, FrontendAppConfig}
import connectors.UserManagementClient._
import connectors.exchange._
import models.UniqueIdentifier
import play.api.http.Status._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserManagementClient @Inject() (config: FrontendAppConfig, http: CSRHttp)(implicit val ec: ExecutionContext) {

  private val role = "candidate" // We have only one role for this application
  private lazy val ServiceName = config.authConfig.serviceName
  private val urlPrefix = "faststream"

  val url = config.userManagementConfig.url

  def register(email: String, password: String, firstName: String, lastName: String)(implicit hc: HeaderCarrier): Future[UserResponse] = {
    http.POST[AddUserRequest, UserResponse](s"${url.host}/$urlPrefix/add",
      AddUserRequest(email.toLowerCase, password, firstName, lastName, List(role), ServiceName)).recover {
        case e: UpstreamErrorResponse if e.statusCode == CONFLICT => throw new EmailTakenException
      }
  }

  def signIn(email: String, password: String)(implicit hc: HeaderCarrier): Future[UserResponse] = {
    http.POST[SignInRequest, HttpResponse](s"${url.host}/$urlPrefix/authenticate", SignInRequest(email.toLowerCase, password, ServiceName))
      .map {
        case resp if resp.status == OK =>
          val response = resp.json.as[UserResponse]
          if (response.roles.head != role) throw new InvalidRoleException() else { response }
        case resp if resp.status == UNAUTHORIZED => throw new InvalidCredentialsException
      }
  }

  def activate(email: String, token: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.POST[ActivateEmailRequest, HttpResponse](s"${url.host}/activate", ActivateEmailRequest(email.toLowerCase, token, ServiceName))
      .map {
        case resp if resp.status == OK => (): Unit
        case resp if resp.status == GONE => throw new TokenExpiredException
        case resp if resp.status == NOT_FOUND => throw new TokenEmailPairInvalidException
      }
  }

  def resendActivationCode(email: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.POST[ResendActivationTokenRequest, HttpResponse](
      s"${url.host}/resend-activation-code", ResendActivationTokenRequest(email.toLowerCase, ServiceName)
    ).map {
      case resp if resp.status == OK => (): Unit
      case resp if resp.status == NOT_FOUND => throw new InvalidEmailException
    }
  }

  def sendResetPwdCode(email: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.POST[SendPasswordCodeRequest, HttpResponse](
      s"${url.host}/$urlPrefix/send-reset-password-code", SendPasswordCodeRequest(email.toLowerCase, ServiceName)
    ).map {
      case resp if resp.status == OK => (): Unit
      case resp if resp.status == NOT_FOUND => throw new InvalidEmailException
    }
  }

  def resetPasswd(email: String, token: String, newPassword: String)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.POST[ResetPasswordRequest, HttpResponse](
      s"${url.host}/$urlPrefix/reset-password",
      ResetPasswordRequest(email.toLowerCase, token, newPassword, ServiceName)
    ).map {
      case resp if resp.status == OK => (): Unit
      case resp if resp.status == GONE => throw new TokenExpiredException
      case resp if resp.status == NOT_FOUND => throw new TokenEmailPairInvalidException
    }
  }

  def updateDetails(userId: UniqueIdentifier, firstName: String, lastName: String, preferredName: Option[String])(
    implicit hc: HeaderCarrier): Future[Unit] =
    http.PUT[UpdateDetails, HttpResponse](
      s"${url.host}/$urlPrefix/service/$ServiceName/details/$userId",
      UpdateDetails(firstName, lastName, preferredName, ServiceName)
    ).map(_ => ())

  def failedLogin(email: String)(implicit hc: HeaderCarrier): Future[UserResponse] = {
    http.PUT[EmailWrapper, UserResponse](s"${url.host}/$urlPrefix/failedAttempt", EmailWrapper(email.toLowerCase, ServiceName)).recover {
      case eNotFound: UpstreamErrorResponse if UpstreamErrorResponse.WithStatusCode.unapply(eNotFound).contains(NOT_FOUND) =>
        throw new InvalidCredentialsException
      case eLocked: UpstreamErrorResponse if UpstreamErrorResponse.WithStatusCode.unapply(eLocked).contains(LOCKED) =>
        throw new AccountLockedOutException
    }
  }

  def find(email: String)(implicit hc: HeaderCarrier): Future[UserResponse] = {
    http.POST[EmailWrapper, UserResponse](s"${url.host}/$urlPrefix/find", EmailWrapper(email.toLowerCase, ServiceName)).recover {
      case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new InvalidCredentialsException
    }
  }

  def findByUserId(userId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[UserResponse] = {
    http.POST[FindByUserIdRequest, UserResponse](s"${url.host}/$urlPrefix/service/$ServiceName/findUserById", FindByUserIdRequest(userId))
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND =>
          throw new InvalidCredentialsException(s"UserId = $userId")
      }
  }
}

object UserManagementClient {
  sealed class InvalidRoleException extends Exception
  sealed class InvalidEmailException extends Exception
  sealed class EmailTakenException extends Exception
  sealed class InvalidCredentialsException(message: String = "") extends Exception(message)
  sealed class AccountLockedOutException extends Exception
  sealed class TokenEmailPairInvalidException extends Exception
  sealed class TokenExpiredException extends Exception
}
