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

package controllers

import config.{FrontendAppConfig, SecurityEnvironment}
import connectors.addresslookup.AddressLookupClient

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import security.Roles.EditPersonalDetailsAndContinueRole
import security.SilhouetteComponent
import uk.gov.hmrc.http.BadRequestException

import scala.concurrent.{ExecutionContext, Future}
import helpers.NotificationTypeHelper

@Singleton
class AddressLookupController @Inject() (
  config: FrontendAppConfig,
  mcc: MessagesControllerComponents,
  val secEnv: SecurityEnvironment,
  val silhouetteComponent: SilhouetteComponent,
  val notificationTypeHelper: NotificationTypeHelper,
  addressLookupClient: AddressLookupClient)(implicit val ec: ExecutionContext) extends BaseController(config, mcc) {

  case class Data(postcode: String)
  object Data {
    implicit val format: OFormat[Data] = Json.format[Data]
  }

  private def isValidPostcode(postcode: String) = {
    // 1 or more alphanumeric characters
    // followed by 0 or 1 spaces
    // followed by 1 or more alphanumeric characters
    postcode.matches("^[a-zA-Z0-9]+[ ]?[a-zA-Z0-9]+$")
  }

  def addressLookupByPostcode: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[Data] { data =>
      val postcode = data.postcode
      if (isValidPostcode(postcode)) {
        val decoded = java.net.URLDecoder.decode(postcode, "UTF8")
        addressLookupClient.findByPostcode(decoded, filter = None).map {
          case head :: tail => Ok(Json.toJson(head :: tail))
          case Nil => NotFound
        }.recover {
          case e: BadRequestException =>
            logger.error(s"Postcode lookup service returned ${e.getMessage} for postcode $postcode")
            BadRequest
        }
      } else {
        Future.successful(BadRequest(s"Invalid postcode:$postcode"))
      }
    }
  }

  def addressLookupByUprn(uprn: String): Action[AnyContent] = CSRSecureAction(EditPersonalDetailsAndContinueRole) {
    implicit request => implicit _cachedData =>
    addressLookupClient.findByUprn(uprn).map(address => Ok(Json.toJson(address)) ).recover {
      case e: BadRequestException =>
        logger.error(s"Postcode lookup service returned ${e.getMessage} for uprn:$uprn")
        BadRequest
    }
  }
}
