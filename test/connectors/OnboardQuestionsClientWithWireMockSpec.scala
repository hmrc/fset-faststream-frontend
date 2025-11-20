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
import connectors.ApplicationClient.CannotUpdateRecord2
import connectors.SchemeClient.{CannotUpdateSchemePreferences, SchemePreferencesNotFound}
import connectors.SchoolsClient.SchoolsNotFound
import connectors.exchange.candidatescores.AssessmentScoresAllExercises
import connectors.exchange.{OnboardQuestions, School, SelectedSchemes}
import models.UniqueIdentifier
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.client.HttpClientV2

import java.util.UUID

class OnboardQuestionsClientWithWireMockSpec extends BaseConnectorWithWireMockSpec {

  val appId = UniqueIdentifier(UUID.randomUUID())
  val base = "candidate-application"
  val endpoint = s"/$base/application/onboard-questions/$appId"

  "findQuestions" should {
    "handle a response indicating success" in new TestFixture {
      stubFor(get(urlEqualTo(endpoint)).willReturn(
        aResponse().withStatus(OK)
          .withBody(Json.toJson(questions).toString())
      ))

      val response = client.findQuestions(appId).futureValue
      response mustBe Some(questions)
    }

    "handle no questions found" in new TestFixture {
      stubFor(get(urlEqualTo(endpoint)).willReturn(
        aResponse().withStatus(NOT_FOUND)
      ))

      val response = client.findQuestions(appId).futureValue
      response mustBe None
    }
  }

  "saveQuestions" should {
    "return unit when OK is received" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withRequestBody(equalTo(Json.toJson(questions).toString()))
        .willReturn(aResponse().withStatus(OK))
      )

      val result = client.saveQuestions(appId, questions).futureValue
      result mustBe unit
    }

    "throw CannotUpdateRecord exception when NOT_FOUND is received" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withRequestBody(equalTo(Json.toJson(questions).toString()))
        .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      val result = client.saveQuestions(appId, questions).failed.futureValue
      result mustBe a[CannotUpdateRecord2]
    }
  }

  trait TestFixture extends BaseConnectorTestFixture {
    val questions = OnboardQuestions(niNumber = "NR123456B")
    val mockConfig = new FrontendAppConfig(mockConfiguration, mockEnvironment) {
      val faststreamUrl = FaststreamBackendUrl(s"http://localhost:$wireMockPort", s"/$base")
      override lazy val faststreamBackendConfig = FaststreamBackendConfig(faststreamUrl)
    }
    val ws = app.injector.instanceOf(classOf[WSClient])
    val http = app.injector.instanceOf(classOf[HttpClientV2])
    val client = new OnboardQuestionsClient(mockConfig, http)
  }
}
