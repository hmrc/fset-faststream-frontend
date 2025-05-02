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

import config.FrontendAppConfig
import connectors.SchemeClient.{CannotUpdateSchemePreferences, SchemePreferencesNotFound}
import connectors.exchange.SelectedSchemes
import models.UniqueIdentifier
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SchemeClient @Inject()(config: FrontendAppConfig, http: HttpClientV2)(implicit ec: ExecutionContext) {
  val apiBaseUrl: String = config.faststreamBackendConfig.url.host + config.faststreamBackendConfig.url.base

  def getSchemePreferences(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier) = {
    http.get(url"$apiBaseUrl/scheme-preferences/$applicationId")
      .execute[SelectedSchemes]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new SchemePreferencesNotFound
      }
  }

  def updateSchemePreferences(data: SelectedSchemes)(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier) = {
    import play.api.libs.ws.writeableOf_JsValue
    http.put(url"$apiBaseUrl/scheme-preferences/$applicationId")
      .withBody(Json.toJson(data))
      .execute[HttpResponse]
      .map {
        case x: HttpResponse if x.status == OK => ()
        case x: HttpResponse if x.status == BAD_REQUEST => throw new CannotUpdateSchemePreferences
      }
  }
}

object SchemeClient {
  sealed class SchemePreferencesNotFound extends Exception
  sealed class CannotUpdateSchemePreferences extends Exception
}
