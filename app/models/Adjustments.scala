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

package models

import play.api.libs.json.{Json, OFormat}

case class Adjustments(adjustments: Option[List[String]],
                        adjustmentsConfirmed: Option[Boolean]) {
  private val approved = adjustmentsConfirmed.contains(true)

  def isInvigilatedETrayApproved: Boolean = adjustments.exists(_.contains("etrayInvigilated")) && approved

  def isInvigilatedVideoApproved: Boolean = adjustments.exists(_.contains("videoInvigilated")) && approved
}

object Adjustments {
  implicit val adjustmentsFormat: OFormat[Adjustments] = Json.format[Adjustments]
}
