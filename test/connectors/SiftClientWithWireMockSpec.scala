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

import com.github.tomakehurst.wiremock.client.WireMock._
import config.{FaststreamBackendConfig, FaststreamBackendUrl, FrontendAppConfig}
import connectors.ApplicationClient._
import connectors.exchange.referencedata.SchemeId
import connectors.exchange.sift.{GeneralQuestionsAnswers, SchemeSpecificAnswer, SiftAnswers, SiftAnswersStatus}
import models.UniqueIdentifier
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.client.HttpClientV2

class SiftClientWithWireMockSpec extends BaseConnectorWithWireMockSpec {

  val applicationId = UniqueIdentifier("5bdbab00-753b-432c-817e-91fb6e1867d3")
  val base = "candidate-application"

  "updateGeneralAnswers" should {
    val endpoint = s"/$base/sift-answers/$applicationId/general"

    val generalQuestionsAnswers = GeneralQuestionsAnswers(
      multipleNationalities = false,
      secondNationality = None,
      nationality = "British",
      undergradDegree = None,
      postgradDegree = None
    )

    "handle a response indicating success" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(generalQuestionsAnswers).toString()))
        .willReturn(aResponse().withStatus(OK))
      )

      val response = client.updateGeneralAnswers(applicationId, generalQuestionsAnswers).futureValue
      response mustBe unit
    }

    "handle a response indicating a bad request" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(generalQuestionsAnswers).toString()))
        .willReturn(aResponse().withStatus(BAD_REQUEST))
      )

      val response = client.updateGeneralAnswers(applicationId, generalQuestionsAnswers).failed.futureValue
      response mustBe a[CannotUpdateRecord]
    }

    "handle a response indicating a conflict" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(generalQuestionsAnswers).toString()))
        .willReturn(aResponse().withStatus(CONFLICT))
      )

      val response = client.updateGeneralAnswers(applicationId, generalQuestionsAnswers).failed.futureValue
      response mustBe a[SiftAnswersSubmitted]
    }

    "not elegantly handle a response indicating an internal server error" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(generalQuestionsAnswers).toString()))
        .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
      )

      val response = client.updateGeneralAnswers(applicationId, generalQuestionsAnswers).failed.futureValue
      response mustBe a[MatchError]
    }
  }

  "updateSchemeSpecificAnswer" should {
    val schemeId = SchemeId("OperationalDelivery")
    val endpoint = s"/$base/sift-answers/$applicationId/${schemeId.value}"

    val schemeSpecificAnswer = SchemeSpecificAnswer("Test answer")

    "handle a response indicating success" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(schemeSpecificAnswer).toString()))
        .willReturn(aResponse().withStatus(OK))
      )

      val response = client.updateSchemeSpecificAnswer(applicationId, schemeId, schemeSpecificAnswer).futureValue
      response mustBe unit
    }

    "handle a response indicating a bad request" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(schemeSpecificAnswer).toString()))
        .willReturn(aResponse().withStatus(BAD_REQUEST))
      )

      val response = client.updateSchemeSpecificAnswer(applicationId, schemeId, schemeSpecificAnswer).failed.futureValue
      response mustBe a[CannotUpdateRecord]
    }

    "handle a response indicating a conflict" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(schemeSpecificAnswer).toString()))
        .willReturn(aResponse().withStatus(CONFLICT))
      )

      val response = client.updateSchemeSpecificAnswer(applicationId, schemeId, schemeSpecificAnswer).failed.futureValue
      response mustBe a[SiftAnswersSubmitted]
    }

    "not elegantly handle a response indicating an internal server error" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(schemeSpecificAnswer).toString()))
        .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
      )

      val response = client.updateSchemeSpecificAnswer(applicationId, schemeId, schemeSpecificAnswer).failed.futureValue
      response mustBe a[MatchError]
    }
  }

  "getGeneralQuestionsAnswers" should {
    val endpoint = s"/$base/sift-answers/$applicationId/general"

    "handle a response that contains data" in new TestFixture {
      val answers = GeneralQuestionsAnswers(
        multipleNationalities = false,
        secondNationality = None,
        nationality = "British",
        undergradDegree = None,
        postgradDegree = None
      )

      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(OK)
          .withBody(Json.toJson(answers).toString())
      ))

      val response = client.getGeneralQuestionsAnswers(applicationId).futureValue
      response mustBe Some(answers)
    }

    "handle a response that contains no data" in new TestFixture {
      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(NOT_FOUND)
      ))

      val response = client.getGeneralQuestionsAnswers(applicationId).futureValue
      response mustBe None
    }
  }


  "getSchemeSpecificAnswer" should {
    val schemeId = SchemeId("Commercial")
    val endpoint = s"/$base/sift-answers/$applicationId/${schemeId.value}"

    "handle a response that contains data" in new TestFixture {
      val schemeSpecificAnswer = SchemeSpecificAnswer("test text")

      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(OK)
          .withBody(Json.toJson(schemeSpecificAnswer).toString())
      ))

      val response = client.getSchemeSpecificAnswer(applicationId, schemeId).futureValue
      response mustBe Some(schemeSpecificAnswer)
    }

    "handle a response that contains no data" in new TestFixture {
      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(NOT_FOUND)
      ))

      val response = client.getSchemeSpecificAnswer(applicationId, schemeId).futureValue
      response mustBe None
    }
  }

  "getSiftAnswers" should {
    val endpoint = s"/$base/sift-answers/$applicationId"

    "handle a response indicating success" in new TestFixture {
      val answers = SiftAnswers(
        applicationId.toString, SiftAnswersStatus.SUBMITTED, generalAnswers = None, schemeAnswers = Map.empty[String, SchemeSpecificAnswer]
      )

      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(OK)
          .withBody(Json.toJson(answers).toString())
      ))

      val response = client.getSiftAnswers(applicationId).futureValue
      response mustBe answers
    }

    "handle a response indicating answers not found" in new TestFixture {
      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(NOT_FOUND)
      ))

      val response = client.getSiftAnswers(applicationId).failed.futureValue
      response mustBe a[SiftAnswersNotFound]
    }
  }

  "submitSiftAnswers" should {
    val endpoint = s"/$base/sift-answers/$applicationId/submit"

    val payload = Array.empty[Byte]

    "handle a response indicating success" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(payload).toString()))
        .willReturn(aResponse().withStatus(OK))
      )

      val response = client.submitSiftAnswers(applicationId).futureValue
      response mustBe unit
    }

    "handle a response indicating an unprocessible entity" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(payload).toString()))
        .willReturn(aResponse().withStatus(UNPROCESSABLE_ENTITY))
      )

      val response = client.submitSiftAnswers(applicationId).failed.futureValue
      response mustBe a[SiftAnswersIncomplete]
    }

    "handle a response indicating a conflict" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(payload).toString()))
        .willReturn(aResponse().withStatus(CONFLICT))
      )

      val response = client.submitSiftAnswers(applicationId).failed.futureValue
      response mustBe a[SiftAnswersSubmitted]
    }

    "handle a response indicating a bad request" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(payload).toString()))
        .willReturn(aResponse().withStatus(BAD_REQUEST))
      )

      val response = client.submitSiftAnswers(applicationId).failed.futureValue
      response mustBe a[SiftAnswersNotFound]
    }

    "handle a response indicating forbidden" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(payload).toString()))
        .willReturn(aResponse().withStatus(FORBIDDEN))
      )

      val response = client.submitSiftAnswers(applicationId).failed.futureValue
      response mustBe a[SiftExpired]
    }

    "not elegantly handle a response indicating an internal server error" in new TestFixture {
      stubFor(put(urlPathEqualTo(endpoint))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(containing(Json.toJson(payload).toString()))
        .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
      )

      val response = client.submitSiftAnswers(applicationId).failed.futureValue
      response mustBe a[MatchError]
    }
  }

  "getSiftAnswersStatus" should {
    val endpoint = s"/$base/sift-answers/$applicationId/status"

    "handle a response that contains data" in new TestFixture {
      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(OK)
        .withBody(Json.toJson(SiftAnswersStatus.SUBMITTED).toString())
      ))

      val response = client.getSiftAnswersStatus(applicationId).futureValue
      response mustBe Some(SiftAnswersStatus.SUBMITTED)
    }

    "handle a response that contains no data" in new TestFixture {
      stubFor(get(urlPathEqualTo(endpoint)).willReturn(
        aResponse().withStatus(NOT_FOUND)
      ))

      val response = client.getSiftAnswersStatus(applicationId).futureValue
      response mustBe None
    }
  }

  trait TestFixture extends BaseConnectorTestFixture {
    val mockConfig = new FrontendAppConfig(mockConfiguration, mockEnvironment) {
      val faststreamUrl = FaststreamBackendUrl(s"http://localhost:$wireMockPort", s"/$base")
      override lazy val faststreamBackendConfig = FaststreamBackendConfig(faststreamUrl)
    }
    val ws = app.injector.instanceOf(classOf[WSClient])
    val http = app.injector.instanceOf(classOf[HttpClientV2])
    val client = new SiftClient(mockConfig, http)
  }
}
