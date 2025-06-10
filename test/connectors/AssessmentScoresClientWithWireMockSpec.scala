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

import com.github.tomakehurst.wiremock.client.WireMock.*
import config.{FaststreamBackendConfig, FaststreamBackendUrl, FrontendAppConfig}
import connectors.SchemeClient.{CannotUpdateSchemePreferences, SchemePreferencesNotFound}
import connectors.exchange.SelectedSchemes
import connectors.exchange.candidatescores.AssessmentScoresAllExercises
import models.UniqueIdentifier
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.client.HttpClientV2

class AssessmentScoresClientWithWireMockSpec extends BaseConnectorWithWireMockSpec {

  val applicationId = UniqueIdentifier("5bdbab00-753b-432c-817e-91fb6e1867d3")
  val base = "candidate-application"

  "findReviewerAcceptedAssessmentScores" should {
    val endpoint = s"/$base/assessment-scores/reviewer/accepted-scores/application/$applicationId"

    "handle a response indicating success" in new TestFixture {
      val assessmentScores = AssessmentScoresAllExercises(applicationId)

      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(OK)
          .withBody(Json.toJson(assessmentScores).toString())
      ))

      val response = client.findReviewerAcceptedAssessmentScores(applicationId).futureValue
      response mustBe assessmentScores
    }

    "handle a response indicating scores not found" in new TestFixture {
      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(NOT_FOUND)
      ))

      val response = client.findReviewerAcceptedAssessmentScores(applicationId).failed.futureValue
      response mustBe an[Exception]
    }

    "handle a response indicating an internal server error" in new TestFixture {
      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(INTERNAL_SERVER_ERROR)
      ))

      val response = client.findReviewerAcceptedAssessmentScores(applicationId).failed.futureValue
      response mustBe an[Exception]
    }
  }

  trait TestFixture extends BaseConnectorTestFixture {
    val mockConfig = new FrontendAppConfig(mockConfiguration, mockEnvironment) {
      val faststreamUrl = FaststreamBackendUrl(s"http://localhost:$wireMockPort", s"/$base")
      override lazy val faststreamBackendConfig = FaststreamBackendConfig(faststreamUrl)
    }
    val ws = app.injector.instanceOf(classOf[WSClient])
    val http = app.injector.instanceOf(classOf[HttpClientV2])
    val client = new AssessmentScoresClient(mockConfig, http)
  }
}
