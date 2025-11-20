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

import config.{FaststreamBackendUrl, FrontendAppConfig}
import connectors.ApplicationClient.*
import connectors.exchange.OnboardQuestions
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
class OnboardQuestionsClient @Inject()(config: FrontendAppConfig, http: HttpClientV2)(implicit ec: ExecutionContext) {

  val url: FaststreamBackendUrl = config.faststreamBackendConfig.url
  val apiBase: String = s"${url.host}${url.base}"

  def findQuestions(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[OnboardQuestions]] = {
    http.get(url"$apiBase/application/onboard-questions/$applicationId").execute[HttpResponse].map { response =>
      response.status match {
        case OK => Some(response.json.as[OnboardQuestions])
        case _ => None
      }
    }
  }

  def saveQuestions(applicationId: UniqueIdentifier, questions: OnboardQuestions)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBase/application/onboard-questions/$applicationId")
      .withBody(Json.toJson(questions))
      .execute[Either[UpstreamErrorResponse, HttpResponse]].map {
        case Right(_) => ()
        case Left(ex) => throw new CannotUpdateRecord2(ex.message)
      }
    }
}
