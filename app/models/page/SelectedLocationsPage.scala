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

package models.page

import connectors.exchange.referencedata.SdipLocation

case class SelectedLocationsPage(refDataLocations: Seq[SdipLocation]) {

  def getValidLocationsByPriority(formData: Map[String, String]) = {
    val selectedLocations = getLocationsByPriority(formData)
    val invalidSchemes = getInvalidLocations(selectedLocations)
    selectedLocations.filterNot(schemeId => invalidSchemes.contains(schemeId))
  }

  // Check the user has not changed the name of a location and tried to submit it
  val getInvalidLocations = (selectedLocations: List[String]) => selectedLocations.diff(refDataLocations.map(_.id.value))

  def getLocationsByPriority(formData: Map[String, String]) = {

    val validLocationParamsFunc = (name: String, value: String) => name.startsWith("location_") && value.nonEmpty
    val extractPriorityNumberFunc: String => Int = _.split("_").last.toInt

    formData.filter { case (name, location) => validLocationParamsFunc(name, location) }
      .collect { case (name, location) => extractPriorityNumberFunc(name) -> location }
      .toList
      .sortBy { case (priority, _) => priority }
      .map { case (_, location) => location }
      .distinct
  }
}
