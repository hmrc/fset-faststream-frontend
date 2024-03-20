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

package connectors.exchange

import play.api.libs.json.{Json, OFormat}

final case class AssistanceDetails(
                                    hasDisability: String,
                                    disabilityImpact: Option[String],
                                    disabilityCategories: Option[List[String]],
                                    otherDisabilityDescription: Option[String],
                                    needsSupportAtVenue: Option[Boolean],
                                    needsSupportAtVenueDescription: Option[String],
                                    needsSupportForPhoneInterview: Option[Boolean], // Used by Edip & Sdip
                                    needsSupportForPhoneInterviewDescription: Option[String], // Used by Edip & Sdip
                                    gis: Option[Boolean] = None //GIS is currently disabled
) {
  override def toString =
    s"hasDisability=$hasDisability," +
      s"disabilityImpact=$disabilityImpact," +
      s"disabilityCategories=$disabilityCategories," +
      s"otherDisabilityDescription=$otherDisabilityDescription," +
      s"needsSupportAtVenue=$needsSupportAtVenue," +
      s"needsSupportAtVenueDescription=$needsSupportAtVenueDescription," +
      s"needsSupportForPhoneInterview=$needsSupportForPhoneInterview," +
      s"needsSupportForPhoneInterviewDescription=$needsSupportForPhoneInterviewDescription," +
      s"gis=$gis"

  def requiresAdjustments: Boolean = {
    List(
      needsSupportAtVenue.contains(true),
      needsSupportForPhoneInterview.contains(true)
    ).contains(true)
  }

  def isDisabledCandidate = hasDisability.toLowerCase == "yes"
  def hasSelectedOtherDisabilityCategory = {
    disabilityCategories.exists(disabilityCategoryList => disabilityCategoryList.contains("Other"))
  }
  def isGis: Boolean = gis.contains(true)
}

object AssistanceDetails {
  implicit val assistanceDetailsFormat: OFormat[AssistanceDetails] = Json.format[AssistanceDetails]
}
