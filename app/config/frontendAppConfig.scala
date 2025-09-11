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

import com.typesafe.config.Config

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import controllers.{ApplicationRouteState, ApplicationRouteStateImpl}

import javax.inject.{Inject, Singleton}
import models.ApplicationRoute._
import play.api.{ConfigLoader, Configuration, Environment, Logging}

case class AuthConfig(serviceName: String)

object AuthConfig {
  implicit val configLoader: ConfigLoader[AuthConfig] = (rootConfig: Config, path: String) => {
    val config = Configuration(rootConfig).get[Configuration](path)
    AuthConfig(config.get[String]("serviceName"))
  }
}

case class UserManagementConfig(url: UserManagementUrl)

object UserManagementConfig {
  implicit val configLoader: ConfigLoader[UserManagementConfig] = (rootConfig: Config, path: String) => {
    val config = Configuration(rootConfig).get[Configuration](path)
    UserManagementConfig(config.get[UserManagementUrl]("url"))
  }
}

case class UserManagementUrl(host: String)

object UserManagementUrl {
  implicit val configLoader: ConfigLoader[UserManagementUrl] = (rootConfig: Config, path: String) => {
    val config = Configuration(rootConfig).get[Configuration](path)
    UserManagementUrl(config.get[String]("host"))
  }
}

case class FaststreamBackendConfig(url: FaststreamBackendUrl)

object FaststreamBackendConfig {
  implicit val configLoader: ConfigLoader[FaststreamBackendConfig] = (rootConfig: Config, path: String) => {
    val config = Configuration(rootConfig).get[Configuration](path)
    FaststreamBackendConfig(config.get[FaststreamBackendUrl]("url"))
  }
}

case class FaststreamBackendUrl(host: String, base: String)

object FaststreamBackendUrl {
  implicit val configLoader: ConfigLoader[FaststreamBackendUrl] = (rootConfig: Config, path: String) => {
    val config = Configuration(rootConfig).get[Configuration](path)
    FaststreamBackendUrl(config.get[String]("host"), config.get[String]("base"))
  }
}

case class ApplicationRouteFrontendConfig(timeZone: Option[String],
                                          startNewAccountsDate: Option[LocalDateTime],
                                          blockNewAccountsDate: Option[LocalDateTime],
                                          blockApplicationsDate: Option[LocalDateTime])

case class AddressLookupConfig(url: String)

object AddressLookupConfig {
  implicit val configLoader: ConfigLoader[AddressLookupConfig] = (rootConfig: Config, path: String) => {
    val config = Configuration(rootConfig).get[Configuration](path)
    AddressLookupConfig(config.get[String]("url"))
  }
}

// Based on the following class:
// https://github.com/hmrc/play-frontend-hmrc/src/main/scala/uk/gov/hmrc/hmrcfrontend/config/TrackingConsentConfig.scala
// to get the GA cookie consent tracking working
case class TrackingConsentConfig(platformHost: Option[String],
                                 trackingConsentHost: Option[String],
                                 trackingConsentPath: Option[String],
                                 gtmContainer: Option[String]
                                ) {
  def trackingUrl(): Option[String] =
    for {
      host <- trackingConsentHost
      path <- trackingConsentPath
      _    <- gtmContainer
    } yield s"$host$path"
}

object ApplicationRouteFrontendConfig {
  def read(timeZone: Option[String], startNewAccountsDate: Option[String], blockNewAccountsDate: Option[String],
           blockApplicationsDate: Option[String]): ApplicationRouteFrontendConfig = {

    def parseDate(dateStr: String): LocalDateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    new ApplicationRouteFrontendConfig(timeZone, startNewAccountsDate map parseDate, blockNewAccountsDate map parseDate,
      blockApplicationsDate map parseDate)
  }
}

@Singleton
class FrontendAppConfig @Inject() (val config: Configuration, val environment: Environment) extends Logging {
  lazy val authConfig = config.get[AuthConfig](s"microservice.services.auth")
  lazy val userManagementConfig = config.get[UserManagementConfig]("microservice.services.user-management")
  lazy val faststreamBackendConfig = config.get[FaststreamBackendConfig]("microservice.services.faststream")

  lazy val addressLookupConfig = config.get[AddressLookupConfig]("microservice.services.address-lookup")

  lazy val fsacGuideUrl = config.get[String]("microservice.fsacGuideUrl")

  lazy val feedbackUrl = config.getOptional[String]("feedback.url").getOrElse("")

  lazy val marketingTrackingEnabled = config.getOptional[Boolean]("marketing.trackingEnabled").getOrElse(false)

  lazy val preGoLiveTestingEnabled = {
    val enabled = config.getOptional[Boolean]("preGoLiveTesting.enabled").getOrElse(false)
    logger.warn(s"Pre go-live testing enabled=$enabled")
    enabled
  }

  lazy val applicationRoutesFrontend = Map(
    Faststream -> loadAppRouteConfig("faststream"),
    Edip -> loadAppRouteConfig("edip"),
    Sdip -> loadAppRouteConfig("sdip"),
    SdipFaststream -> loadAppRouteConfig("faststream")
  )

  private lazy val platformHost: Option[String] =
    config.getOptional[String]("platform.frontend.host")
  private lazy val trackingConsentHost: Option[String] =
    platformHost.map(_ => "").orElse(config.getOptional[String]("tracking-consent-frontend.host"))
  private lazy val trackingConsentPath: Option[String] = config.getOptional[String]("tracking-consent-frontend.path")
  private lazy val gtmContainer: Option[String] = config.getOptional[String]("tracking-consent-frontend.gtm.container")

  logger.warn(s"GA platformHost=$platformHost")
  logger.warn(s"GA trackingConsentHost=$trackingConsentHost")
  logger.warn(s"GA trackingConsentPath=$trackingConsentPath")
  logger.warn(s"GA gtmContainer=$gtmContainer")

  lazy val trackingConsentConfig = TrackingConsentConfig(platformHost, trackingConsentHost, trackingConsentPath, gtmContainer)

  def loadAppRouteConfig(routeKey: String): ApplicationRouteState = {
    val timeZone = config.getOptional[String]("applicationRoute.timeZone")
    val startNewAccountsDate = config.getOptional[String](s"applicationRoute.$routeKey.startNewAccountsDate")
    val blockNewAccountsDate = config.getOptional[String](s"applicationRoute.$routeKey.blockNewAccountsDate")
    val blockApplicationsDate = config.getOptional[String](s"applicationRoute.$routeKey.blockApplicationsDate")
    logger.warn(s"Reading campaign closing times timeZone=$timeZone")
    logger.warn(s"Reading campaign closing times for routeKey=$routeKey...")
    logger.warn(s"routeKey=$routeKey - startNewAccountsDate=$startNewAccountsDate")
    logger.warn(s"routeKey=$routeKey - blockNewAccountsDate=$blockNewAccountsDate")
    logger.warn(s"routeKey=$routeKey - blockApplicationsDate=$blockApplicationsDate")

    ApplicationRouteStateImpl(
      ApplicationRouteFrontendConfig.read(
        timeZone = config.getOptional[String]("applicationRoute.timeZone"),
        startNewAccountsDate = config.getOptional[String](s"applicationRoute.$routeKey.startNewAccountsDate"),
        blockNewAccountsDate = config.getOptional[String](s"applicationRoute.$routeKey.blockNewAccountsDate"),
        blockApplicationsDate = config.getOptional[String](s"applicationRoute.$routeKey.blockApplicationsDate")
      )
    )
  }

  // Whitelist Configuration
  private def whitelistConfig(key: String): Seq[String] = Some(
    new String(Base64.getDecoder.decode(config.getOptional[String](key).getOrElse("")), "UTF-8")
  ).map(_.split(",")).getOrElse(Array.empty[String]).toSeq

  lazy val whitelist = whitelistConfig("whitelist")
  lazy val whitelistFileUpload = whitelistConfig("whitelistFileUpload")
  lazy val whitelistExcluded = whitelistConfig("whitelistExcludedCalls")
}
