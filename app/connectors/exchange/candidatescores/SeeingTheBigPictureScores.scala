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

package connectors.exchange.candidatescores

import play.api.libs.json.{Json, OFormat}

// TODO: this class can be deleted
case class SeeingTheBigPictureScores(
                                      strategicOutlook: Option[Double] = None,
                                      understandsHowOptionsMeetDiverseNeedsOfStakeholders: Option[Double] = None,
                                      suggestsWaysToImproveAcceptance: Option[Double] = None,
                                      respondsAppropriately: Option[Double] = None,
                                      alternativeSolutions: Option[Double] = None
)

object SeeingTheBigPictureScores {
  implicit val seeingTheBigPictureScoresFormat: OFormat[SeeingTheBigPictureScores] = Json.format[SeeingTheBigPictureScores]
}
