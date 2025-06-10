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
import connectors.exchange.referencedata.SchemeId
import connectors.exchange.sift.SiftAnswersStatus.SiftAnswersStatus
import connectors.exchange.sift.{GeneralQuestionsAnswers, SchemeSpecificAnswer, SiftAnswers}
import models.UniqueIdentifier
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SiftClient @Inject() (config: FrontendAppConfig, http: HttpClientV2)(implicit ec: ExecutionContext) {

  val url: FaststreamBackendUrl = config.faststreamBackendConfig.url
  val apiBase: String = s"${url.host}${url.base}"

  def updateGeneralAnswers(applicationId: UniqueIdentifier, answers: GeneralQuestionsAnswers)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBase/sift-answers/$applicationId/general")
      .withBody(Json.toJson(answers))
      .execute[HttpResponse]
      .map {
        case response if response.status == OK => ()
        case response if response.status == BAD_REQUEST => throw new CannotUpdateRecord
        case response if response.status == CONFLICT => throw new SiftAnswersSubmitted
      }
  }

  def updateSchemeSpecificAnswer(applicationId: UniqueIdentifier, schemeId: SchemeId, answer: SchemeSpecificAnswer)
                                (implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBase/sift-answers/$applicationId/${schemeId.value}")
      .withBody(Json.toJson(answer))
      .execute[HttpResponse]
      .map {
        case response if response.status == OK => ()
        case response if response.status == BAD_REQUEST => throw new CannotUpdateRecord
        case response if response.status == CONFLICT => throw new SiftAnswersSubmitted
      }
  }

  def getGeneralQuestionsAnswers(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[GeneralQuestionsAnswers]] = {
    http.get(url"$apiBase/sift-answers/$applicationId/general")
      .execute[Option[GeneralQuestionsAnswers]]
  }

  def getSchemeSpecificAnswer(applicationId: UniqueIdentifier, schemeId: SchemeId)
    (implicit hc: HeaderCarrier): Future[Option[SchemeSpecificAnswer]] = {
    http.get(url"$apiBase/sift-answers/$applicationId/${schemeId.value}")
      .execute[Option[SchemeSpecificAnswer]]
  }

  def getSiftAnswers(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[SiftAnswers] = {
    http.get(url"$apiBase/sift-answers/$applicationId")
      .execute[SiftAnswers]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND => throw new SiftAnswersNotFound()
      }
  }

  // scalastyle:off cyclomatic.complexity
  def submitSiftAnswers(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Unit] = {
    http.put(url"$apiBase/sift-answers/$applicationId/submit")
      .withBody(Json.toJson(Array.empty[Byte]))
      .execute[HttpResponse]
      .map {
        case response if response.status == OK => ()
        case response if response.status == UNPROCESSABLE_ENTITY => throw new SiftAnswersIncomplete
        case response if response.status == CONFLICT => throw new SiftAnswersSubmitted
        case response if response.status == BAD_REQUEST => throw new SiftAnswersNotFound
        case response if response.status == FORBIDDEN => throw new SiftExpired
      }
  }
  // scalastyle:on

  def getSiftAnswersStatus(applicationId: UniqueIdentifier)(implicit hc: HeaderCarrier): Future[Option[SiftAnswersStatus]] = {
    http.get(url"$apiBase/sift-answers/$applicationId/status")
      .execute[Option[SiftAnswersStatus]]
  }
}
