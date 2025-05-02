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
import models.UniqueIdentifier
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.client.HttpClientV2

class SchemeClientWithWireMockSpec extends BaseConnectorWithWireMockSpec {

  val applicationId = UniqueIdentifier("5bdbab00-753b-432c-817e-91fb6e1867d3")
  val base = "candidate-application"

  "getSchemePreferences" should {
    val endpoint = s"/$base/scheme-preferences/$applicationId"

    "handle a response indicating success" in new TestFixture {
      val schemes = SelectedSchemes(List("Commercial"), orderAgreed = true, eligible = true)

      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(OK)
          .withBody(Json.toJson(schemes).toString())
      ))

      val response = client.getSchemePreferences(applicationId).futureValue
      response mustBe schemes
    }

    "handle a response indicating schemes preferences not found" in new TestFixture {
      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(NOT_FOUND)
      ))

      val response = client.getSchemePreferences(applicationId).failed.futureValue
      response mustBe a[SchemePreferencesNotFound]
    }
  }

  "updateSchemePreferences" should {
    val endpoint = s"/$base/scheme-preferences/$applicationId"
    val schemes = SelectedSchemes(List("Commercial"), orderAgreed = true, eligible = true)

    "handle a response indicating success" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(OK)
          .withBody(Json.toJson(schemes).toString())
      ))

      val response = client.updateSchemePreferences(schemes)(applicationId).futureValue
      response mustBe unit
    }

    "handle a response indicating a bad request" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(schemes).toString()))
        .willReturn(aResponse().withStatus(BAD_REQUEST))
      )

      val response = client.updateSchemePreferences(schemes)(applicationId).failed.futureValue
      response mustBe a[CannotUpdateSchemePreferences]
    }
  }
  
  trait TestFixture extends BaseConnectorTestFixture {
    val mockConfig = new FrontendAppConfig(mockConfiguration, mockEnvironment) {
      val faststreamUrl = FaststreamBackendUrl(s"http://localhost:$wireMockPort", s"/$base")
      override lazy val faststreamBackendConfig = FaststreamBackendConfig(faststreamUrl)
    }
    val ws = app.injector.instanceOf(classOf[WSClient])
    val http = app.injector.instanceOf(classOf[HttpClientV2])
    val client = new SchemeClient(mockConfig, http)
  }
}
