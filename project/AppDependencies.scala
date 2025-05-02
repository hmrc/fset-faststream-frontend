/*
 * Copyright 2016 HM Revenue & Customs
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

import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  object Versions {
    val bootstrapVersion  = "9.11.0"
    val silhouetteVersion = "10.0.1"
  }

  import Versions.*

  val compile = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30"               % bootstrapVersion,
    "uk.gov.hmrc"                   %% "http-caching-client-play-30"              % "12.1.0",
    "org.playframework.silhouette"  %% "play-silhouette"                          % silhouetteVersion,
    "org.playframework.silhouette"  %% "play-silhouette-password-bcrypt"          % silhouetteVersion,
    "org.playframework.silhouette"  %% "play-silhouette-crypto-jca"               % silhouetteVersion,
    "org.playframework.silhouette"  %% "play-silhouette-persistence"              % silhouetteVersion,
    "net.codingwell"                %% "scala-guice"                              % "6.0.0",
    ws
  )

  val test = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-test-play-30"   % bootstrapVersion  % Test,
    "org.playframework.silhouette"  %% "play-silhouette-testkit"  % silhouetteVersion % Test
  )

  def apply() = compile ++ test
}
