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

import forms.FastPassForm.*
import models.ApplicationRoute
import play.api.data.{Form, FormError}

class FastPassFormSpec extends BaseFormSpec {

  def fastPassForm: Form[Data] = FastPassForm.form

  "FastPass form" should {
    "be valid when candidate is not applicable" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "false"
      ))
      form.hasErrors mustBe false
      form.hasGlobalErrors mustBe false
      form.value.get mustBe Data("false")
    }

    "be valid when candidate is applicable and is a civil servant with no fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "Cabinet Office",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe false
      form.hasGlobalErrors mustBe false
      // Check the submitted data has been correctly converted
      form.value.get mustBe Data(applicable = "true",
        civilServantAndInternshipTypes = Some(Seq(FastPassForm.CivilServantKey)),
        civilServantDepartment = Some("Cabinet Office"),
        liveDisciplinaryWarning = Some(false),
        inReviewPeriodFollowingAWarning = Some(false),
        inImprovementPeriodFollowingAWarning = Some(false),
        fastPassReceived = Some(false)
      )
    }

    "be valid when candidate is applicable, chooses all internship options and has a fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes[0]" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantAndInternshipTypes[1]" -> FastPassForm.SDIPKey,
        "civilServiceExperienceDetails.civilServantAndInternshipTypes[2]" -> FastPassForm.EDIPKey,
        "civilServiceExperienceDetails.civilServantAndInternshipTypes[3]" -> FastPassForm.OtherInternshipKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "Cabinet Office",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.sdipYear" -> "2020",
        "civilServiceExperienceDetails.edipYear" -> "2020",
        "civilServiceExperienceDetails.otherInternshipName" -> "Internship name",
        "civilServiceExperienceDetails.otherInternshipYear" -> "2020",
        "civilServiceExperienceDetails.fastPassReceived" -> "true",
        "civilServiceExperienceDetails.certificateNumber" -> "1234567"
      ))
      form.hasErrors mustBe false
      form.hasGlobalErrors mustBe false

      // Check the submitted data has been correctly converted
      val data = form.value.get
      val postedCivilServantAndInternshipTypes = data.civilServantAndInternshipTypes.get
      val dataWithoutStream = data.copy(civilServantAndInternshipTypes = None)

      dataWithoutStream mustBe Data(applicable = "true",
        civilServantAndInternshipTypes = None,
        civilServantDepartment = Some("Cabinet Office"),
        liveDisciplinaryWarning = Some(false),
        inReviewPeriodFollowingAWarning = Some(false),
        inImprovementPeriodFollowingAWarning = Some(false),
        sdipYear = Some("2020"),
        edipYear = Some("2020"),
        otherInternshipName = Some("Internship name"),
        otherInternshipYear = Some("2020"),
        fastPassReceived = Some(true),
        certificateNumber = Some("1234567")
      )

      postedCivilServantAndInternshipTypes
        .diff(Seq(FastPassForm.CivilServantKey, FastPassForm.SDIPKey, FastPassForm.EDIPKey, FastPassForm.OtherInternshipKey))
        .isEmpty mustBe true
    }

    "be invalid when candidate is applicable and is a civil servant with no fast pass but doesn't specify their department" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.civilServantDepartment",
        message = "error.civilServantDepartment.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable and is a civil servant with no fast pass and specifies an invalid department" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "BOOM",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.civilServantDepartment",
        message = "error.civilServantDepartment.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable and is a civil servant with no fast pass and doesn't answer the live disciplinary warning" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "Cabinet Office",
        "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.liveDisciplinaryWarning",
        message = "error.liveDisciplinaryWarning.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable and is a civil servant with no fast pass and posts an invalid answer to the " +
      "live disciplinary warning" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "Cabinet Office",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "BOOM",
        "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.liveDisciplinaryWarning",
        message = "error.liveDisciplinaryWarning.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable and is a civil servant with no fast pass and answers the live disciplinary warning with a yes" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "Cabinet Office",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "true",
        "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.liveDisciplinaryWarning",
        message = "error.liveDisciplinaryWarning.cannot.continue"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable and is a civil servant with no fast pass and doesn't answer the " +
      "in review period following a warning" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "Cabinet Office",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning",
        message = "error.inReviewPeriodFollowingAWarning.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable and is a civil servant with no fast pass and posts an invalid answer to the " +
      "in review period following a warning" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "Cabinet Office",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning" -> "BOOM",
        "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning",
        message = "error.inReviewPeriodFollowingAWarning.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable and is a civil servant with no fast pass and answers the " +
      "in review period following a warning with a yes" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "Cabinet Office",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning" -> "true",
        "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning",
        message = "error.inReviewPeriodFollowingAWarning.cannot.continue"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable and is a civil servant with no fast pass and doesn't answer the " +
      "in improvement period following a warning" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "Cabinet Office",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning",
        message = "error.inImprovementPeriodFollowingAWarning.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable and is a civil servant with no fast pass and posts an invalid answer to the " +
      "in improvement period following a warning" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "Cabinet Office",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning" -> "BOOM",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning",
        message = "error.inImprovementPeriodFollowingAWarning.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable and is a civil servant with no fast pass and answers the " +
      "in improvement period following a warning with a yes" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "Cabinet Office",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "civilServiceExperienceDetails.inReviewPeriodFollowingAWarning" -> "false",
        "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning" -> "true",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.inImprovementPeriodFollowingAWarning",
        message = "error.inImprovementPeriodFollowingAWarning.cannot.continue"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    // Edip
    "be valid when candidate is not applicable, but specifies edip internship but doesn't supply the year and has no fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "false",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.EDIPKey,
        "civilServiceExperienceDetails.fastPassReceived" -> "false"))
      form.hasErrors mustBe false
      form.hasGlobalErrors mustBe false
      // Check the submitted data has been correctly converted
      form.value.get mustBe Data(applicable = "false")
    }

    "be invalid when candidate is applicable, has completed the edip internship but doesn't supply the year and has no fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.EDIPKey,
        "civilServiceExperienceDetails.fastPassReceived" -> "false"))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.edipYear",
        message = "error.edipInternshipYear.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable, has completed the edip internship, supplies an invalid year and has no fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.EDIPKey,
        "civilServiceExperienceDetails.edipYear" -> "BOOM",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.edipYear",
        message = "error.edipInternshipYear.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable, has completed the edip internship, supplies an invalid 5 digit year and has no fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.EDIPKey,
        "civilServiceExperienceDetails.edipYear" -> "12345",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.edipYear",
        message = "error.edipInternshipYear.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be valid when candidate is applicable, has completed the edip internship, supplies a valid year and has no fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.EDIPKey,
        "civilServiceExperienceDetails.edipYear" -> "2020",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"))
      form.hasErrors mustBe false
      form.hasGlobalErrors mustBe false
      // Check the submitted data has been correctly converted
      form.value.get mustBe Data(applicable = "true",
        civilServantAndInternshipTypes = Some(Seq(FastPassForm.EDIPKey)),
        edipYear = Some("2020"),
        fastPassReceived = Some(false)
      )
    }

    // Sdip
    "be valid when candidate is not applicable, but specifies sdip internship but doesn't supply the year and has no fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "false",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.SDIPKey,
        "civilServiceExperienceDetails.fastPassReceived" -> "false"))
      form.hasErrors mustBe false
      form.hasGlobalErrors mustBe false
      // Check the submitted data has been correctly converted
      form.value.get mustBe Data(applicable = "false")
    }

    "be invalid when candidate is applicable, has completed sdip internship but doesn't supply the year and has no fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.SDIPKey,
        "civilServiceExperienceDetails.fastPassReceived" -> "false"))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.sdipYear",
        message = "error.sdipInternshipYear.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable, has completed sdip internship, supplies an invalid year and has no fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.SDIPKey,
        "civilServiceExperienceDetails.sdipYear" -> "BOOM",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.sdipYear",
        message = "error.sdipInternshipYear.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable, has completed sdip internship, supplies an invalid 5 digit year and has no fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.SDIPKey,
        "civilServiceExperienceDetails.sdipYear" -> "12345",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.sdipYear",
        message = "error.sdipInternshipYear.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be valid when candidate is applicable, has completed sdip internship, supplies a valid year and has no fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.SDIPKey,
        "civilServiceExperienceDetails.sdipYear" -> "2020",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"))
      form.hasErrors mustBe false
      form.hasGlobalErrors mustBe false
      // Check the submitted data has been correctly converted
      form.value.get mustBe Data(applicable = "true",
        civilServantAndInternshipTypes = Some(Seq(FastPassForm.SDIPKey)),
        sdipYear = Some("2020"),
        fastPassReceived = Some(false)
      )
    }

    // Other internship
    "be valid when candidate is not applicable, but specifies another internship but doesn't supply the name or year and has no fast pass" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "false",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes" -> FastPassForm.OtherInternshipKey,
        "civilServiceExperienceDetails.fastPassReceived" -> "false"))
      form.hasErrors mustBe false
      form.hasGlobalErrors mustBe false
      // Check the submitted data has been correctly converted
      form.value.get mustBe Data(applicable = "false")
    }

    "be invalid when candidate is applicable, chooses other internship fails to enter a name or a year" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes[0]" -> FastPassForm.OtherInternshipKey,
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(
        FormError(key = "civilServiceExperienceDetails.otherInternshipName", message = "error.otherInternshipName.required"),
        FormError(key = "civilServiceExperienceDetails.otherInternshipYear", message = "error.otherInternshipYear.required")
      )
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable, chooses other internship and enters a name that is too big" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes[0]" -> FastPassForm.OtherInternshipKey,
        "civilServiceExperienceDetails.otherInternshipName" -> "A" * (FastPassForm.otherInternshipNameMaxSize + 1),
        "civilServiceExperienceDetails.otherInternshipYear" -> "2020",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.otherInternshipName",
        message = "error.otherInternshipName.size"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }

    "be invalid when candidate is applicable, chooses other internship and enters a year that is not a number" in {
      val form = fastPassForm.bind(Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes[0]" -> FastPassForm.OtherInternshipKey,
        "civilServiceExperienceDetails.otherInternshipName" -> "Internship name",
        "civilServiceExperienceDetails.otherInternshipYear" -> "BOOM",
        "civilServiceExperienceDetails.fastPassReceived" -> "false"
      ))
      form.hasErrors mustBe true
      val expectedFormErrors = Seq(FormError(key = "civilServiceExperienceDetails.otherInternshipYear",
        message = "error.otherInternshipYear.required"))
      form.errors mustBe expectedFormErrors
      form.hasGlobalErrors mustBe false
    }
  }

  "cleanup fast pass params" should {
    "remove other fields when candidate is not applicable" in {
      val data = Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "false",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes[0]" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantAndInternshipTypes[1]" -> FastPassForm.SDIPKey,
        "civilServiceExperienceDetails.civilServantAndInternshipTypes[2]" -> FastPassForm.EDIPKey,
        "civilServiceExperienceDetails.civilServantAndInternshipTypes[3]" -> FastPassForm.OtherInternshipKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "ABC",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "civilServiceExperienceDetails.sdipYear" -> "2020",
        "civilServiceExperienceDetails.edipYear" -> "2020",
        "civilServiceExperienceDetails.otherInternshipName" -> "Internship name",
        "civilServiceExperienceDetails.otherInternshipYear" -> "2020",
        "civilServiceExperienceDetails.fastPassReceived" -> "true",
        "civilServiceExperienceDetails.certificateNumber" -> "1234567"
      )
      data.cleanupFastPassFields mustBe Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "false"
      )
    }

    "remove all 3rd level data when the choices are off" in {
      val data = Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantDepartment" -> "ABC",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "civilServiceExperienceDetails.sdipYear" -> "2020",
        "civilServiceExperienceDetails.edipYear" -> "2020",
        "civilServiceExperienceDetails.otherInternshipName" -> "Internship name",
        "civilServiceExperienceDetails.otherInternshipYear" -> "2020",
        "civilServiceExperienceDetails.fastPassReceived" -> "false",
        "civilServiceExperienceDetails.certificateNumber" -> "1234567")
      data.cleanupFastPassFields must contain theSameElementsAs
        Map(
          "applicationRoute" -> ApplicationRoute.Faststream.toString,
          "civilServiceExperienceDetails.applicable" -> "true",
          "civilServiceExperienceDetails.fastPassReceived" -> "false"
        )
    }

    "remove certificate number when fast pass is not received" in {
      val data = Map(
        "applicationRoute" -> ApplicationRoute.Faststream.toString,
        "civilServiceExperienceDetails.applicable" -> "true",
        "civilServiceExperienceDetails.civilServantAndInternshipTypes[0]" -> FastPassForm.CivilServantKey,
        "civilServiceExperienceDetails.civilServantDepartment" -> "ABC",
        "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
        "fastPassReceived" -> "false",
        "certificateNumber" -> "1234567")
      data.cleanupFastPassFields must contain theSameElementsAs
        Map(
          "applicationRoute" -> ApplicationRoute.Faststream.toString,
          "civilServiceExperienceDetails.applicable" -> "true",
          "civilServiceExperienceDetails.civilServantAndInternshipTypes[0]" -> FastPassForm.CivilServantKey,
          "civilServiceExperienceDetails.civilServantDepartment" -> "ABC",
          "civilServiceExperienceDetails.liveDisciplinaryWarning" -> "false",
          "fastPassReceived" -> "false"
        )
    }
  }
}
