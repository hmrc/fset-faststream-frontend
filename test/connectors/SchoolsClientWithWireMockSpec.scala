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
import connectors.SchoolsClient.SchoolsNotFound
import connectors.exchange.{School, SelectedSchemes}
import connectors.exchange.candidatescores.AssessmentScoresAllExercises
import models.UniqueIdentifier
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.client.HttpClientV2

class SchoolsClientWithWireMockSpec extends BaseConnectorWithWireMockSpec {

  val term = "term"
  val base = "candidate-application"

  "getSchools" should {
    val endpoint = s"/$base/schools?term=$term"

    "handle a response indicating success" in new TestFixture {
      val schools = List(
        School(typeId = "", id = "", name = "")
      )

      stubFor(get(urlEqualTo(endpoint)).willReturn(
        aResponse().withStatus(OK)
          .withBody(Json.toJson(schools).toString())
      ))

      val response = client.getSchools(term).futureValue
      response mustBe schools
    }

    "handle a response indicating schools not found" in new TestFixture {
      stubFor(get(urlEqualTo(endpoint)).willReturn(
        aResponse().withStatus(NOT_FOUND)
      ))

      val response = client.getSchools(term).failed.futureValue
      response mustBe a[SchoolsNotFound]
    }
  }

  trait TestFixture extends BaseConnectorTestFixture {
    val mockConfig = new FrontendAppConfig(mockConfiguration, mockEnvironment) {
      val faststreamUrl = FaststreamBackendUrl(s"http://localhost:$wireMockPort", s"/$base")
      override lazy val faststreamBackendConfig = FaststreamBackendConfig(faststreamUrl)
    }
    val ws = app.injector.instanceOf(classOf[WSClient])
    val http = app.injector.instanceOf(classOf[HttpClientV2])
    val client = new SchoolsClient(mockConfig, http)
  }
}
