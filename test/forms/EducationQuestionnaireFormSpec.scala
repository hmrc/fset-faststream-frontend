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

import forms.EducationQuestionnaireFormExamples._
import play.api.data.{Form, FormError}
import play.api.i18n.Messages

class EducationQuestionnaireFormSpec extends BaseFormSpec {

  "the education form" should {
    "be valid when all values are correct" in new Fixture {
      val (data, form) = FullValid
      form.get mustBe data
    }

    "be valid when all possible values are <<I don't know/ prefer not to say>>" in new Fixture {
      val (data, form) = AllPreferNotToSayValid
      form.get mustBe data
    }

    "be valid when not lived in UK and have degree" in new Fixture {
      val (data, form) = NoUkAndHasDegreeValid
      form.get mustBe data
    }

    "be valid when live in UK and no degree" in new Fixture {
      val (data, form) = LiveInUKAndNoDegreeValid
      form.get mustBe data
    }

    "be valid when not lived in UK and no degree" in new Fixture {
      val (data, form) = NoUkLivedAndNoDegreeValid
      form.get mustBe data
    }

    "fail when all values are correct but not lived in UK" in new Fixture {
      assertFieldRequired(FullValidFormMap, "liveInUKBetween14and18", "liveInUKBetween14and18")
    }

    "fail when all values are correct but no postcodeQ" in new Fixture {
      assertFieldRequired(FullValidFormMap, "postcodeQ", "postcodeQ")
    }

    "fail when all values are correct but no schoolName14to16" in new Fixture {
      assertFieldRequired(FullValidFormMap, "schoolName14to16", "schoolName14to16")
    }

    "fail when all values are correct but no schoolName16to18" in new Fixture {
      assertFieldRequired(FullValidFormMap, "schoolName16to18", "schoolName16to18")
    }

    "fail when all values are correct but no freeSchoolMeals" in new Fixture {
      assertFieldRequired(FullValidFormMap, "freeSchoolMeals", "freeSchoolMeals")
    }

    "fail when all values are correct but no university" in new Fixture {
      assertFieldRequired(FullValidFormMap, "university", "university")
    }

    "fail when all values are correct but no universityDegreeCategory" in new Fixture {
      assertFieldRequired(FullValidFormMap, "universityDegreeCategory", "universityDegreeCategory")
    }

    "fail when all values are correct but no degreeType" in new Fixture {
      assertFieldRequired(FullValidFormMap, "degreeType", "degreeType")
    }

    "fail when all values are correct but no hasPostgradDegree" in new Fixture {
      assertFieldRequired(FullValidFormMap, "hasPostgradDegree", "hasPostgradDegree")
    }

    "fail when all values are correct but no postgradUniversity.university" in new Fixture {
      assertFieldRequired(FullValidFormMap, "postgradUniversity.university", "postgradUniversity.university")
    }

    "fail when all values are correct but no postgradUniversity.degreeType" in new Fixture {
      assertFieldRequired(FullValidFormMap, "postgradUniversity.degreeType", "postgradUniversity.degreeType")
    }

    "fail when all values are correct but no postgradUniversity.degreeCategory" in new Fixture {
      assertFieldRequired(FullValidFormMap, "postgradUniversity.degreeCategory", "postgradUniversity.degreeCategory")
    }

    "succeed for an edip candidate who should not be presented the hasPostgradDegree question" in new Fixture {
      val invalidForm = edipForm.bind(NotUkLivedAndHasDegreeValidFormMap - "hasPostgradDegree")
      invalidForm.hasErrors mustBe false
    }

    "fail when all values are correct and not lived in UK and have degree but no university, with Fast Stream message" in new Fixture {
      val invalidForm = fastStreamForm.bind(NotUkLivedAndHasDegreeValidFormMap - "university")
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("university") mustBe Some(
        FormError("university", List(Messages("error.university.required"))
      ))
    }

    "fail when submitted university is invalid" in new Fixture {
      val invalidForm = fastStreamForm.bind(NotUkLivedAndHasDegreeValidFormMap + ("university" -> "BOOM"))
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("university") mustBe Some(
        FormError("university", List(Messages("error.university.invalid"))
      ))
    }

    "fail when submitted postgrad university is invalid" in new Fixture {
      val invalidForm = fastStreamForm.bind(FullValidFormMap + ("postgradUniversity.university" -> "BOOM"))
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("postgradUniversity.university") mustBe Some(
        FormError("postgradUniversity.university", List(Messages("error.postgradUniversity.university.invalid"))
      ))
    }

    "fail when submitted university degree category is invalid" in new Fixture {
      val invalidForm = fastStreamForm.bind(NotUkLivedAndHasDegreeValidFormMap + ("universityDegreeCategory" -> "BOOM"))
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("universityDegreeCategory") mustBe Some(
        FormError("universityDegreeCategory", List(Messages("error.universityDegreeCategory.invalid"))
        ))
    }

    "fail when submitted postgrad university degree category is invalid" in new Fixture {
      val invalidForm = fastStreamForm.bind(FullValidFormMap + ("postgradUniversity.degreeCategory" -> "BOOM"))
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("postgradUniversity.degreeCategory") mustBe Some(
        FormError("postgradUniversity.degreeCategory", List(Messages("error.postgradUniversity.degreeCategory.invalid"))
        ))
    }

    "fail when all values are correct and not lived in UK and have degree but no degree type specified" in new Fixture {
      val invalidForm = fastStreamForm.bind(NotUkLivedAndHasDegreeValidFormMap - "degreeType")
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("degreeType") mustBe Some(
        FormError("degreeType", List(Messages("error.degreeType.required"))
      ))
    }

    "fail when all values are correct and not lived in UK and have postgrad degree but no postgrad degree type specified" in new Fixture {
      val invalidForm = fastStreamForm.bind(FullValidFormMap - "postgradUniversity.degreeType")
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("postgradUniversity.degreeType") mustBe Some(
        FormError("postgradUniversity.degreeType", List(Messages("error.postgradUniversity.degreeType.required"))
      ))
    }

    "fail when the degree type value posted is invalid" in new Fixture {
      val invalidForm = fastStreamForm.bind(NotUkLivedAndHasDegreeValidFormMap + ("degreeType" -> "BOOM"))
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("degreeType") mustBe Some(
        FormError("degreeType", List(Messages("error.degreeType.invalid"))
      ))
    }

    "fail when the postgrad degree type value posted is invalid" in new Fixture {
      val invalidForm = fastStreamForm.bind(FullValidFormMap + ("postgradUniversity.degreeType" -> "BOOM"))
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("postgradUniversity.degreeType") mustBe Some(
        FormError("postgradUniversity.degreeType", List(Messages("error.postgradUniversity.degreeType.invalid"))
      ))
    }

    "fail when the degree type is Other but no other details are submitted" in new Fixture {
      val invalidForm = fastStreamForm.bind(NotUkLivedAndHasDegreeValidFormMap + ("degreeType" -> "Other"))
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("otherDegreeType") mustBe Some(
        FormError("otherDegreeType", List(Messages("error.otherDegreeType.required"))
      ))
    }

    "fail when the postgrad degree type is Other but no other details are submitted" in new Fixture {
      val invalidForm = fastStreamForm.bind(FullValidFormMap + ("postgradUniversity.degreeType" -> "Other"))
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("postgradUniversity.otherDegreeType") mustBe Some(
        FormError("postgradUniversity.otherDegreeType", List(Messages("error.postgradUniversity.otherDegreeType.required"))
      ))
    }

    "fail when the degree type is Other and the other details provided exceed the max size" in new Fixture {
      val invalidForm = fastStreamForm.bind(NotUkLivedAndHasDegreeValidFormMap
        + ("degreeType" -> "Other", "otherDegreeType" -> "A" * (EducationQuestionnaireForm.TextMaxSize + 1)))
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("otherDegreeType") mustBe Some(
        FormError("otherDegreeType", List(Messages("error.otherDegreeType.maxLength"))
      ))
    }

    "fail when the postgrad degree type is Other and the other details provided exceed the max size" in new Fixture {
      val invalidForm = fastStreamForm.bind(FullValidFormMap
        + ("postgradUniversity.degreeType" -> "Other",
        "postgradUniversity.otherDegreeType" -> "A" * (EducationQuestionnaireForm.TextMaxSize + 1))
      )
      invalidForm.hasErrors mustBe true
      invalidForm.errors.size mustBe 1
      invalidForm.error("postgradUniversity.otherDegreeType") mustBe Some(
        FormError("postgradUniversity.otherDegreeType", List(Messages("error.postgradUniversity.otherDegreeType.maxLength"))
      ))
    }

    "fail when all values are correct and not lived in UK and have degree but no university, with EDIP message" in new Fixture {
      val invalidForm = edipForm.bind(NotUkLivedAndHasDegreeValidFormMap - "university")
      invalidForm.hasErrors mustBe true
      invalidForm.error("university") mustBe Some(
        FormError("university", List(Messages("error.university.required"))
      ))
    }

    "fail when all values are correct and live in UK and no degree but no school" in new Fixture {
      assertFieldRequired(LivedInUKAndNoDegreeValidFormMap, "schoolName14to16", "schoolName14to16")
    }

    "transform form when form is full valid (has degree and lived in uk) to a question list" in new Fixture {
      val questionList = FullValidForm.toExchange(true)(mockMessages).questions
      questionList.size mustBe 14
      questionList.head.answer.answer mustBe Some("Yes")
      questionList.head.answer.unknown mustBe None
      questionList(1).answer.answer mustBe Some("AAA 111")
      questionList(1).answer.unknown mustBe None
      questionList(2).answer.answer mustBe Some("my school at 15")
      questionList(2).answer.unknown mustBe None
      questionList(3).answer.answer mustBe Some("state funded")
      questionList(3).answer.unknown mustBe None
      questionList(4).answer.answer mustBe Some("my school at 17")
      questionList(4).answer.unknown mustBe None
      questionList(5).answer.answer mustBe Some("No")
      questionList(5).answer.unknown mustBe None
      questionList(6).answer.answer mustBe Some("Yes")
      questionList(6).answer.unknown mustBe None
      questionList(7).answer.answer mustBe Some("A14-AWC")
      questionList(7).answer.unknown mustBe None
      questionList(8).answer.answer mustBe Some("Chemistry")
      questionList(8).answer.unknown mustBe None
      questionList(9).answer.answer mustBe Some("BSc/MSc/Eng")
      questionList(9).answer.unknown mustBe None
      questionList(10).answer.answer mustBe Some("Yes")
      questionList(10).answer.unknown mustBe None
      questionList(11).answer.answer mustBe Some("K12-KEELE")
      questionList(11).answer.unknown mustBe None
      questionList(12).answer.answer mustBe Some("Computing")
      questionList(12).answer.unknown mustBe None
      questionList(13).answer.answer mustBe Some("BSc/MSc/Eng")
      questionList(13).answer.unknown mustBe None
    }

    "transform form when has degree with all possible fields with prefer not to say" in new Fixture {
      val questionList = AllPreferNotToSayValidForm.toExchange(true)(mockMessages).questions
      questionList.size mustBe 11
      questionList.head.answer.answer mustBe Some("Yes")
      questionList.head.answer.unknown mustBe None
      questionList(1).answer.answer mustBe None
      questionList(1).answer.unknown mustBe Some(true)
      questionList(2).answer.answer mustBe None
      questionList(2).answer.unknown mustBe Some(true)
      questionList(3).answer.answer mustBe Some("I don't know/prefer not to say")
      questionList(4).answer.answer mustBe None
      questionList(4).answer.unknown mustBe Some(true)
      questionList(5).answer.answer mustBe None
      questionList(5).answer.unknown mustBe Some(true)
      questionList(6).answer.answer mustBe Some("Yes")
      questionList(6).answer.unknown mustBe None
      questionList(7).answer.answer mustBe None
      questionList(7).answer.unknown mustBe None
      questionList(8).answer.answer mustBe None
      questionList(8).answer.unknown mustBe None
      questionList(9).answer.answer mustBe Some("BSc/MSc/Eng")
      questionList(9).answer.unknown mustBe None
      questionList(10).answer.answer mustBe Some("No")
      questionList(10).answer.unknown mustBe None
    }

    "transform form with no lived in uk and has not degree with valid fields to a question list" in new Fixture {
      val questionList = NotUkLivedAndNoDegreeValidForm.toExchange(true)(mockMessages).questions
      questionList.size mustBe 2
      questionList.head.answer.answer mustBe Some("No")
      questionList.head.answer.unknown mustBe None
      questionList(1).answer.answer mustBe Some("No")
      questionList(1).answer.unknown mustBe None
    }

    "transform form when has degree and no uk lived with all valid fields to a question list" in new Fixture {
      val questionList = NotUkLivedAndHasDegreeValidForm.toExchange(true)(mockMessages).questions
      questionList.size mustBe 6
      questionList.head.answer.answer mustBe Some("No")
      questionList.head.answer.unknown mustBe None
      questionList(1).answer.answer mustBe Some("Yes")
      questionList(1).answer.unknown mustBe None
      questionList(2).answer.answer mustBe Some("A14-AWC")
      questionList(2).answer.unknown mustBe None
      questionList(3).answer.answer mustBe Some("Chemistry")
      questionList(3).answer.unknown mustBe None
      questionList(4).answer.answer mustBe Some("BSc/MSc/Eng")
      questionList(4).answer.unknown mustBe None
      questionList(5).answer.answer mustBe Some("No")
      questionList(5).answer.unknown mustBe None
    }

    "transform form for a non fast stream candidate who has a degree and not lived in uk to a question list" in new Fixture {
      val questionList = NotUkLivedAndHasDegreeValidForm.toExchange(false)(mockMessages).questions
      questionList.size mustBe 5
      questionList.head.answer.answer mustBe Some("No")
      questionList.head.answer.unknown mustBe None
      questionList(1).answer.answer mustBe Some("Yes")
      questionList(1).answer.unknown mustBe None
      questionList(2).answer.answer mustBe Some("A14-AWC")
      questionList(2).answer.unknown mustBe None
      questionList(3).answer.answer mustBe Some("Chemistry")
      questionList(3).answer.unknown mustBe None
      questionList(4).answer.answer mustBe Some("BSc/MSc/Eng")
      questionList(4).answer.unknown mustBe None
    }

    "transform form when no degree but lived in UK with all valid fields to a question list" in new Fixture {
      val questionList = LivedInUKAndNoDegreeValidForm.toExchange(true)(mockMessages).questions
      questionList.size mustBe 7
      questionList.head.answer.answer mustBe Some("Yes")
      questionList.head.answer.unknown mustBe None
      questionList(1).answer.answer mustBe Some("AAA 111")
      questionList(1).answer.unknown mustBe None
      questionList(2).answer.answer mustBe Some("my school at 15")
      questionList(2).answer.unknown mustBe None
      questionList(3).answer.answer mustBe Some("state funded")
      questionList(3).answer.unknown mustBe None
      questionList(4).answer.answer mustBe Some("my school at 17")
      questionList(4).answer.unknown mustBe None
      questionList(5).answer.answer mustBe Some("No")
      questionList(5).answer.unknown mustBe None
      questionList(6).answer.answer mustBe Some("No")
      questionList(6).answer.unknown mustBe None
    }

    "sanitize data should respect values when liveInUKBetween14and18 is Yes and hasDegree is Yes" in new Fixture {
      EducationQuestionnaireFormExamples.FullValidForm.sanitizeData mustBe EducationQuestionnaireFormExamples.FullValidForm
    }

    "sanitize data should sanitize values when liveInUKBetween14and18 is No and hasDegree is No" in new Fixture {
      EducationQuestionnaireFormExamples.NotUkLivedAndNotHasDegreeFullInvalidForm.sanitizeData mustBe
        EducationQuestionnaireFormExamples.NotUkLivedAndNoDegreeValidForm
    }
  }

  trait Fixture {
    val formWrapper = new EducationQuestionnaireForm

    val fastStreamForm = formWrapper.form(true, "university")(mockMessages)
    val edipForm = formWrapper.form(false, "currentUniversity")(mockMessages)

    val FullValid = (EducationQuestionnaireFormExamples.FullValidForm, fastStreamForm.fill(
      EducationQuestionnaireFormExamples.FullValidForm))

    val AllPreferNotToSayValid = (EducationQuestionnaireFormExamples.AllPreferNotToSayValidForm, fastStreamForm.fill(
      EducationQuestionnaireFormExamples.AllPreferNotToSayValidForm))

    val NoUkAndHasDegreeValid = (EducationQuestionnaireFormExamples.NotUkLivedAndHasDegreeValidForm, fastStreamForm.fill(
      EducationQuestionnaireFormExamples.NotUkLivedAndHasDegreeValidForm))

    val LiveInUKAndNoDegreeValid = (EducationQuestionnaireFormExamples.LivedInUKAndNoDegreeValidForm, fastStreamForm.fill(
      EducationQuestionnaireFormExamples.LivedInUKAndNoDegreeValidForm))

    val NoUkLivedAndNoDegreeValid = (EducationQuestionnaireFormExamples.NotUkLivedAndNoDegreeValidForm,
      fastStreamForm.fill(EducationQuestionnaireFormExamples.NotUkLivedAndNoDegreeValidForm))

    val EdipNoUkLivedAndNoDegreeValid = (EducationQuestionnaireFormExamples.NotUkLivedAndNoDegreeValidForm,
      edipForm.fill(EducationQuestionnaireFormExamples.NotUkLivedAndNoDegreeValidForm))

    def assertFieldRequired(formMap: Map[String, String], expectedKeyInError: String, fieldKey: String*) =
      assertFormError(expectedKeyInError, formMap ++ fieldKey.map(k => k -> ""), fastStreamForm)

    def assertFormError(expectedKey: String, invalidFormValues: Map[String, String], form: Form[EducationQuestionnaireForm.Data]) = {
      val invalidForm = form.bind(invalidFormValues)
      invalidForm.hasErrors mustBe true
      invalidForm.errors.map(_.key) mustBe Seq(expectedKey)
    }
  }
}
