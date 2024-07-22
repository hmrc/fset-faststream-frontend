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

package forms

import connectors.ReferenceDataExamples

class SelectedLocationsFormSpec extends BaseFormSpec {

  def selectedLocationsForm =
    new SelectedLocationsForm(ReferenceDataExamples.Locations.AllLocations).form

  "Selected locations form" should {
    "be valid when required values are supplied" in {
       val form = selectedLocationsForm.bind(Map(
         "location_0" -> "location1",
         "interests[0]" -> "Interest 1"
       ))
       form.hasErrors mustBe false
       form.hasGlobalErrors mustBe false
    }

    "be valid when multiple locations and interests are selected" in {
      val form = selectedLocationsForm.bind(Map(
        "location_0" -> "location4",
        "location_1" -> "location3",
        "location_2" -> "location2",
        "location_3" -> "location1",
        "interests[0]" -> "Interest 1",
        "interests[1]" -> "Interest 2"
      ))
      form.hasErrors mustBe false
      form.hasGlobalErrors mustBe false
    }

    "be invalid when locations are not supplied" in {
      val form = selectedLocationsForm.bind(Map("interests[0]" -> "Interest 1"))
      form.hasErrors mustBe true
      form.errors.head.message mustBe "locations.required"
      form.hasGlobalErrors mustBe false
    }

    "be invalid when interests are not supplied" in {
      val form = selectedLocationsForm.bind(Map("location_0" -> "location1"))
      form.hasErrors mustBe true
      form.errors.head.message mustBe "You must choose an interest"
      form.hasGlobalErrors mustBe false
    }

    "be invalid when invalid locations are supplied" in {
      val form = selectedLocationsForm.bind(Map(
        "location_0" -> "InvalidLocation",
        "interests[0]" -> "Interest 1"
      ))
      form.hasErrors mustBe true
      form.errors.head.message mustBe "locations.required"
      form.hasGlobalErrors mustBe false
    }

    "be invalid when invalid interests are supplied" in {
      val form = selectedLocationsForm.bind(Map(
        "location_0" -> "location1",
        "interests[0]" -> "InvalidInterest"
      ))
      form.hasErrors mustBe true
      form.errors.head.message mustBe "You must choose an interest"
      form.hasGlobalErrors mustBe false
    }

    "be invalid when locations exceed the maximum" in {
      val form = selectedLocationsForm.bind(Map(
        "location_0" -> "location1",
        "location_1" -> "location2",
        "location_2" -> "location3",
        "location_3" -> "location4",
        "location_4" -> "location5",
        "interests[0]" -> "Interest 1"
      ))
      form.hasErrors mustBe true
      form.errors.map(_.message) mustBe List("locations.tooMany")
      form.hasGlobalErrors mustBe false
    }
  }
}
