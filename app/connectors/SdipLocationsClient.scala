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
import connectors.SdipLocationsClient.{CannotUpdateLocationPreferences, LocationPreferencesNotFound}
import connectors.exchange.SelectedLocations
import models.UniqueIdentifier
import play.api.http.Status._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SdipLocationsClient @Inject() (config: FrontendAppConfig, http: CSRHttp)(implicit ec: ExecutionContext) {
  val url = config.faststreamBackendConfig.url

  def getLocationPreferences(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier) = {
    http.GET[SelectedLocations](
      s"${url.host}${url.base}/location-preferences/$applicationId"
    ).recover {
      case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new LocationPreferencesNotFound
    }
  }

  def updateLocationPreferences(data: SelectedLocations)(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier) = {
    http.PUT[SelectedLocations, HttpResponse](
      s"${url.host}${url.base}/location-preferences/$applicationId",
      data
    ).map {
      case x: HttpResponse if x.status == OK => ()
      case x: HttpResponse if x.status == BAD_REQUEST => throw new CannotUpdateLocationPreferences
    }
  }
}

object SdipLocationsClient {
  sealed class LocationPreferencesNotFound extends Exception
  sealed class CannotUpdateLocationPreferences extends Exception
}
