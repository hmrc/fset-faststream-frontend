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

import connectors.exchange.referencedata.{Degree, LocationId, Scheme, SchemeId, SdipLocation, SiftRequirement}

object ReferenceDataExamples {

  object Schemes {
    val Commercial = Scheme("Commercial", "CFS", "Commercial", civilServantEligible = false, degree = None, Some(SiftRequirement.NUMERIC_TEST),
      siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val DDTaC = Scheme("Digital", "DDTAC", "Digital", civilServantEligible = false,
      degree = None, Some(SiftRequirement.FORM), siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val Dip = Scheme("DiplomaticAndDevelopment", "DS", "Diplomatic and Development", civilServantEligible = false, degree = None,
      Some(SiftRequirement.FORM), siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val DipEcon = Scheme("DiplomaticAndDevelopmentEconomics", "GES-DS", "Diplomatic and Development Economics", civilServantEligible = false,
      degree = None, Some(SiftRequirement.FORM), siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val Finance = Scheme("Finance", "FIFS", "Finance", civilServantEligible = false, degree = None, Some(SiftRequirement.NUMERIC_TEST),
      siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val GovComms = Scheme("GovernmentCommunicationService", "GCFS", "Government Communication Service", civilServantEligible = false,
      degree = None, Some(SiftRequirement.FORM),  siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val GovEconomics = Scheme("GovernmentEconomicsService", "GES", "Government Economics Service", civilServantEligible = false,
      degree = None, Some(SiftRequirement.FORM), siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val GovOps = Scheme("GovernmentOperationalResearchService", "GORS", "Government Operational Research Service", civilServantEligible = false,
      degree = None, Some(SiftRequirement.FORM), siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val GovPolicy = Scheme("GovernmentPolicy", "GP", "Government Policy", civilServantEligible = false,
      degree = None, siftRequirement = None, siftEvaluationRequired = false, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val GovSocialResearch = Scheme("GovernmentSocialResearchService", "GSR", "Government Social Research Service", civilServantEligible = false,
      degree = None, Some(SiftRequirement.FORM), siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val GovStats = Scheme("GovernmentStatisticalService", "GSS", "Government Statistical Service", civilServantEligible = false,
      degree = None, Some(SiftRequirement.FORM), siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val HoP = Scheme("HousesOfParliament", "HoP", "Houses Of Parliament", civilServantEligible = false, degree = None,
      Some(SiftRequirement.FORM), siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val HR = Scheme("HumanResources", "HRFS", "Human Resources", civilServantEligible = false, degree = None, siftRequirement = None,
      siftEvaluationRequired = false, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val OperationalDelivery = Scheme("OperationalDelivery", "OPD", "Operational Delivery", civilServantEligible = false, degree = None,
      siftRequirement = None, siftEvaluationRequired = false, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val ProjectDelivery = Scheme("ProjectDelivery", "PDFS", "Project Delivery", civilServantEligible = false,
      degree = Some(Degree(required = "Degree_CharteredEngineer", specificRequirement = true)),
      Some(SiftRequirement.FORM), siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val SciEng = Scheme("ScienceAndEngineering", "SEFS", "Science And Engineering", civilServantEligible = false, degree = None,
      Some(SiftRequirement.FORM), siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val Edip = Scheme("Edip", "EDIP", "Early Diversity Internship Programme", civilServantEligible = false, degree = None,
      Some(SiftRequirement.FORM), siftEvaluationRequired = true, fsbType = None, schemeGuide = None, schemeQuestion = None)
    val Sdip = Scheme("Sdip", "SDIP", "Summer Diversity Internship Programme", civilServantEligible = false, degree = None,
      Some(SiftRequirement.FORM), siftEvaluationRequired = false, fsbType = None, schemeGuide = None, schemeQuestion = None)

    val AllSchemes = (Commercial :: DDTaC :: Dip :: DipEcon :: Finance :: OperationalDelivery :: GovComms ::
      GovEconomics :: GovOps :: GovPolicy :: GovSocialResearch :: GovStats :: HoP :: HR :: ProjectDelivery :: SciEng ::
      Edip :: Sdip :: Nil)
      .filterNot( s => s.id == SchemeId("GovernmentCommunicationService")) // Filter out GFCS for 2021 campaign

    val SomeSchemes = Commercial :: DDTaC :: Dip :: Nil

    def schemesWithNoDegree = AllSchemes.filter( _.degree.isEmpty )
  }

  object Locations {
    val Location1 = SdipLocation(LocationId("London"), "London")
    val Location2 = SdipLocation(LocationId("Manchester"), "Manchester")
    val Location3 = SdipLocation(LocationId("Newcastle"), "Newcastle")
    val Location4 = SdipLocation(LocationId("Reading"), "Reading")
    val Location5 = SdipLocation(LocationId("York"), "York")

    val AllLocations = (Location1 :: Location2 :: Location3 :: Location4 :: Location5 :: Nil)
  }
}
