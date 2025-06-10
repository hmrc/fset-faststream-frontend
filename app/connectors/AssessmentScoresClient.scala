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
import connectors.exchange.candidatescores.AssessmentScoresAllExercises
import models.UniqueIdentifier
import play.api.http.Status._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AssessmentScoresClient @Inject() (config: FrontendAppConfig, http: HttpClientV2)(implicit ec: ExecutionContext) {

  val url = config.faststreamBackendConfig.url
  val apiBase: String = s"${url.host}${url.base}"

  def findReviewerAcceptedAssessmentScores(applicationId: UniqueIdentifier)(
    implicit hc: HeaderCarrier): Future[AssessmentScoresAllExercises] = {

    http.get(url"$apiBase/assessment-scores/reviewer/accepted-scores/application/$applicationId")
      .execute[AssessmentScoresAllExercises]
      .recover {
        case e: UpstreamErrorResponse if e.statusCode == NOT_FOUND =>
          throw new Exception(s"Error no assessment scores found for application id $applicationId")
        case _ => throw new Exception(s"Error retrieving assessment scores for application id $applicationId")
      }
  }
}
