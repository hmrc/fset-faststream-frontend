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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, ConfigLoader, Configuration, Environment}
import testkit.BaseSpec

trait BaseConnectorWithWireMockSpec extends BaseSpec with BeforeAndAfterEach with BeforeAndAfterAll with GuiceOneServerPerSuite {

  protected def wireMockPort: Int = 11111
  private val stubHost = "localhost"

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Map(
      "microservice.services.faststream.url.host" -> s"http://localhost:$wireMockPort"
    ))
    .build()

  protected val wiremockBaseUrl: String = s"http://localhost:$wireMockPort"
  protected val wireMockServer = new WireMockServer(wireMockConfig().port(wireMockPort))

  override def beforeAll() = {
    wireMockServer.stop()
    wireMockServer.start()
    WireMock.configureFor(stubHost, wireMockPort)
  }

  override def afterAll() = {
    wireMockServer.stop()
  }

  override def beforeEach() = {
    WireMock.reset()
  }

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(15, Millis))

  trait BaseConnectorTestFixture {
    val mockConfiguration = mock[Configuration]
    when(mockConfiguration.getOptional(any[String])(any[ConfigLoader[String]])).thenReturn(None)
    val mockEnvironment = mock[Environment]
  }
}
