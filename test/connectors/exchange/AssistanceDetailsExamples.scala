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

import forms.AssistanceDetailsForm

object AssistanceDetailsExamples {

  val OnlyDisabilityNoGisNoAdjustments = AssistanceDetails(
    hasDisability = "Yes", disabilityImpact = Some("No"),
    disabilityCategories = Some(List(AssistanceDetailsForm.disabilityCategoriesList.head)),
    otherDisabilityDescription = None,
    needsSupportAtVenue = Some(false), needsSupportAtVenueDescription = None,
    needsSupportForPhoneInterview = None, needsSupportForPhoneInterviewDescription = None
  )

  val DisabilityGisAndAdjustments = AssistanceDetails(
    hasDisability = "Yes", disabilityImpact = Some("No"),
    disabilityCategories = Some(List(AssistanceDetailsForm.disabilityCategoriesList.head)),
    otherDisabilityDescription = None,
    needsSupportAtVenue = Some(true), needsSupportAtVenueDescription = Some("Some fsac adjustments"),
    needsSupportForPhoneInterview = None, needsSupportForPhoneInterviewDescription = None
  )

  // Note the functionality to identify that a candidate has been invited to an incorrect number of tests
  // needs to distinguish between a GIS candidate and a regular candidate
  // Even though GIS is currently disabled, we keep the functionality in case we want to use it again in a future campaign
  val DisabilityGisAndAdjustments2 = AssistanceDetails(
    hasDisability = "Yes", disabilityImpact = Some("No"),
    disabilityCategories = Some(List(AssistanceDetailsForm.disabilityCategoriesList.head)),
    otherDisabilityDescription = None,
    needsSupportAtVenue = Some(true), needsSupportAtVenueDescription = Some("Some fsac adjustments"),
    needsSupportForPhoneInterview = None, needsSupportForPhoneInterviewDescription = None, gis = Some(true)
  )

  val EdipAdjustments = AssistanceDetails(
    hasDisability = "Yes", disabilityImpact = Some("No"),
    disabilityCategories = Some(List(AssistanceDetailsForm.disabilityCategoriesList.head)), otherDisabilityDescription = None,
    needsSupportAtVenue = None, needsSupportAtVenueDescription = None, needsSupportForPhoneInterview = Some(true),
    needsSupportForPhoneInterviewDescription = Some("Some phone adjustments"))

  val SdipAdjustments = AssistanceDetails(
    hasDisability = "Yes", disabilityImpact = Some("No"),
    disabilityCategories = Some(List(AssistanceDetailsForm.disabilityCategoriesList.head)), otherDisabilityDescription = None,
    needsSupportAtVenue = None, needsSupportAtVenueDescription = None,
    needsSupportForPhoneInterview = Some(true), needsSupportForPhoneInterviewDescription = Some("Some phone adjustments")
  )
}
