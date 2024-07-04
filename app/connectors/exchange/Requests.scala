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

package connectors.exchange

import models.ApplicationRoute.ApplicationRoute
import models.UniqueIdentifier
import play.api.libs.json.{Json, OFormat}

case class FindByUserIdRequest(userId: UniqueIdentifier)
object FindByUserIdRequest {
  implicit val format: OFormat[FindByUserIdRequest] = Json.format[FindByUserIdRequest]
}

case class CreateApplicationRequest(userId: UniqueIdentifier, frameworkId: String, applicationRoute: ApplicationRoute)
object CreateApplicationRequest {
  implicit val format: OFormat[CreateApplicationRequest] = Json.format[CreateApplicationRequest]
}

case class AddUserRequest(email: String, password: String, firstName: String, lastName: String, roles: List[String], service: String)
object AddUserRequest {
  implicit val format: OFormat[AddUserRequest] = Json.format[AddUserRequest]
}

case class UpdateUserRequest(email: String, password: String, firstName: String, lastName: String,
                             userId: UniqueIdentifier, isActive: Boolean, service: String)
object UpdateUserRequest {
  implicit val format: OFormat[UpdateUserRequest] = Json.format[UpdateUserRequest]
}

case class SignInRequest(email: String, password: String, service: String)
object SignInRequest {
  implicit val format: OFormat[SignInRequest] = Json.format[SignInRequest]
}

case class FindUserRequest(email: String)
object FindUserRequest {
  implicit val format: OFormat[FindUserRequest] = Json.format[FindUserRequest]
}

case class ActivateEmailRequest(email: String, token: String, service: String)
object ActivateEmailRequest {
  implicit val format: OFormat[ActivateEmailRequest] = Json.format[ActivateEmailRequest]
}

case class ResendActivationTokenRequest(email: String, service: String)
object ResendActivationTokenRequest {
  implicit val format: OFormat[ResendActivationTokenRequest] = Json.format[ResendActivationTokenRequest]
}

case class SendPasswordCodeRequest(email: String, service: String)
object SendPasswordCodeRequest {
  implicit val format: OFormat[SendPasswordCodeRequest] = Json.format[SendPasswordCodeRequest]
}

case class ResetPasswordRequest(email: String, token: String, newPassword: String, service: String)
object ResetPasswordRequest {
  implicit val format: OFormat[ResetPasswordRequest] = Json.format[ResetPasswordRequest]
}

case class PreviewRequest(flag: Boolean)
object PreviewRequest {
  implicit val format: OFormat[PreviewRequest] = Json.format[PreviewRequest]
}

final case class VerifyInvigilatedTokenUrlRequest(email: String, accessCode: String)
object VerifyInvigilatedTokenUrlRequest {
  implicit val format: OFormat[VerifyInvigilatedTokenUrlRequest] = Json.format[VerifyInvigilatedTokenUrlRequest]
}
