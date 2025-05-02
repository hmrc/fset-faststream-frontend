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

package config

import play.api.Application
import play.api.mvc.{LegacySessionCookieBaker, RequestHeader}
import play.silhouette.api.crypto.{Base64AuthenticatorEncoder, Hash}
import play.silhouette.api.util.{Clock, FingerprintGenerator}
import play.silhouette.api.{Environment, EventBus}
import play.silhouette.impl.authenticators.{SessionAuthenticatorService, SessionAuthenticatorSettings}
import security.{CsrCredentialsProvider, UserCacheService}
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

object CaseInSensitiveFingerPrintGenerator extends FingerprintGenerator {
  import play.api.http.HeaderNames._
  def generate(implicit request: RequestHeader) = {
    Hash.sha1(new StringBuilder()
      .append(request.headers.get(USER_AGENT).map(x => x.toLowerCase).getOrElse("")).append(":")
      .append(request.headers.get(ACCEPT_LANGUAGE).map(_.toLowerCase).getOrElse("")).append(":")
      .append(request.headers.get(ACCEPT_CHARSET).map(_.toLowerCase).getOrElse("")).append(":")
      .toString()
    )
  }
}

@Singleton
class SecurityEnvironmentImpl @Inject() (
                                          val application: Application,
                                          val config: FrontendAppConfig,
                                          val http: HttpClientV2,
                                          val userCacheService: UserCacheService,
                                          val sessionCookieBaker: LegacySessionCookieBaker)(
  implicit val ec: ExecutionContext) extends SecurityEnvironment {
  lazy val eventBus: EventBus = EventBus()

  val userService = userCacheService
  def identityService = userCacheService

  private val sessionAuthenticationSettings = SessionAuthenticatorSettings(
    sessionKey = application.configuration.getOptional[String]("silhouette.authenticator.sessionKey").get,
    useFingerprinting = application.configuration.getOptional[Boolean]("silhouette.authenticator.useFingerprinting").get,
    authenticatorIdleTimeout = application.configuration.getOptional[Int](
      "silhouette.authenticator.authenticatorIdleTimeout").map(x => x seconds),
    authenticatorExpiry = application.configuration.getOptional[Int](
      "silhouette.authenticator.authenticatorExpiry").get seconds
  )

  val authenticatorService = new SessionAuthenticatorService(
    sessionAuthenticationSettings,
    CaseInSensitiveFingerPrintGenerator,
    new Base64AuthenticatorEncoder,
    sessionCookieBaker,
    Clock())

  val credentialsProvider = new CsrCredentialsProvider(config, http)

  def providers = Map(credentialsProvider.id -> credentialsProvider)

  def requestProviders = Nil

  val executionContext = ec
}

trait SecurityEnvironment extends Environment[security.SecurityEnvironment] {
  val application: Application
  val config: FrontendAppConfig
  val http: HttpClientV2
  def userCacheService: UserCacheService
  val sessionCookieBaker: LegacySessionCookieBaker
  implicit val ec: ExecutionContext

  val credentialsProvider: CsrCredentialsProvider
  val authenticatorService: SessionAuthenticatorService
  val userService: UserCacheService
  def identityService: UserCacheService
}
