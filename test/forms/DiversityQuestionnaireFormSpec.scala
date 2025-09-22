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

import forms.DiversityQuestionnaireForm.{AcceptanceTerms, Data}

class DiversityQuestionnaireFormSpec extends BaseFormSpec {

  "the acceptance form" should {
    "be valid when the accept value is correct for a faststream candidate" in new AcceptanceFixture {
      val validForm = faststreamForm.bind(validFormValues)
      val expectedData = validFormData
      val actualData = validForm.get
      actualData mustBe expectedData
    }

    "fail when no accept value is specified for a faststream candidate" in new AcceptanceFixture {
      assertFaststreamFormFieldRequired(expectedError = "accept-terms", "accept-terms")
    }

    "be valid when the accept value is correct for a sdip candidate" in new AcceptanceFixture {
      val validForm = sdipForm.bind(validFormValues)
      val expectedData = validFormData
      val actualData = validForm.get
      actualData mustBe expectedData
    }

    "fail when no accept value is specified for a sdip candidate" in new AcceptanceFixture {
      assertSdipFormFieldRequired(expectedError = "accept-terms", "accept-terms")
    }
  }

  "the diversity form" should {
    "be valid when all values are correct" in new Fixture {
      val validForm = formWrapper.bind(validFormValues)
      val expectedData = validFormData
      val actualData = validForm.get
      actualData mustBe expectedData
    }

    "fail when no sex is specified" in new Fixture {
      assertFieldRequired(expectedError = "sex", "sex")
    }

    "fail when sex is not a correct value" in new Fixture {
      assertFormError("sex", validFormValues ++ Seq("sex" -> "BOOM"))
    }

    "fail when no sexOrientation is specified" in new Fixture {
      assertFieldRequired(expectedError = "sexOrientation", "sexOrientation")
    }

    "fail when sexOrientation is not a correct value" in new Fixture {
      assertFormError("sexOrientation", validFormValues ++ Seq("sexOrientation" -> "BOOM"))
    }

    "fail when other sexOrientation is too big" in new Fixture {
      assertFormError("sexOrientation", validFormValues ++ Seq("sexOrientation" -> "A" * (DiversityQuestionnaireForm.OtherMaxSize + 1)))
    }

    "be valid when ethnicity is specified and preferNotToSay is not selected" in new Fixture {
      val validForm = formWrapper.bind(validFormValues ++ Seq("ethnicity" -> "Irish", "preferNotSay_ethnicity" -> ""))
      val expectedData = Data(
        sex = "Male",
        sexOrientation = "Other", otherSexOrientation = Some("details"),
        ethnicity = Some("Irish"), otherEthnicity = None, preferNotSayEthnicity = None,
        isEnglishFirstLanguage = "Yes"
      )
      val actualData = validForm.get
      actualData mustBe expectedData
    }

    "fail when no ethnicity is specified and preferNotToSay is not selected" in new Fixture {
      assertFieldRequired(expectedError = "ethnicity", validFormValues ++ Seq("preferNotSay_ethnicity" -> ""))
    }

    "fail when ethnicity is not a correct value and preferNotToSay is not selected" in new Fixture {
      assertFormError("ethnicity", validFormValues ++ Seq("ethnicity" -> "BOOM", "preferNotSay_ethnicity" -> ""))
    }

    "be valid when ethnicity is not a correct value and preferNotToSay is selected" in new Fixture {
      val validForm = formWrapper.bind(validFormValues ++ Seq("ethnicity" -> "BOOM"))
      val expectedData = Data(
        sex = "Male",
        sexOrientation = "Other", otherSexOrientation = Some("details"),
        ethnicity = None, otherEthnicity = None, preferNotSayEthnicity = Some(true),
        isEnglishFirstLanguage = "Yes"
      )
      val actualData = validForm.get
      actualData mustBe expectedData
    }

    "fail when other ethnicity is too big" in new Fixture {
      assertFormError("other_ethnicity", validFormValues ++ Seq("other_ethnicity" -> "A" * (DiversityQuestionnaireForm.OtherMaxSize + 1)))
    }

    "fail when preferNotSay_ethnicity is not a correct value" in new Fixture {
      assertFormError("preferNotSay_ethnicity", validFormValues ++ Seq("preferNotSay_ethnicity" -> "BOOM"))
    }

    "fail when no language is specified" in new Fixture {
      assertFieldRequired(expectedError = "isEnglishFirstLanguage", "isEnglishFirstLanguage")
    }

    "fail when English language is not a correct value" in new Fixture {
      assertFormError("isEnglishFirstLanguage", validFormValues ++ Seq("isEnglishFirstLanguage" -> "BOOM"))
    }

    "transform properly to a question list" in new Fixture {
      val questionList = validFormData.exchange.questions
      questionList.size mustBe 4
      questionList(0).answer.answer mustBe Some("Male")
      questionList(1).answer.otherDetails mustBe Some("details")
      questionList(2).answer.unknown mustBe Some(true)
      questionList(3).answer.answer mustBe Some("Yes")
    }
  }

  trait AcceptanceFixture {

    val validFormData = AcceptanceTerms(acceptTerms = true)

    val validFormValues = Map(
      "accept-terms" -> "true"
    )

    val faststreamForm = new DiversityQuestionnaireForm().acceptanceForm(isFastStream = true)
    val sdipForm = new DiversityQuestionnaireForm().acceptanceForm(isFastStream = false)

    def assertFaststreamFormFieldRequired(expectedError: String, fieldKeysToClear: String*) =
      assertFaststreamFormError(expectedError, validFormValues ++ fieldKeysToClear.map(k => k -> ""))

    def assertFaststreamFormError(expectedKey: String, invalidFormValues: Map[String, String]) = {
      val invalidForm = faststreamForm.bind(invalidFormValues)
      invalidForm.hasErrors mustBe true
      invalidForm.errors.map(_.key) mustBe Seq(expectedKey)
    }

    def assertSdipFormFieldRequired(expectedError: String, fieldKeysToClear: String*) =
      assertSdipFormError(expectedError, validFormValues ++ fieldKeysToClear.map(k => k -> ""))

    def assertSdipFormError(expectedKey: String, invalidFormValues: Map[String, String]) = {
      val invalidForm = sdipForm.bind(invalidFormValues)
      invalidForm.hasErrors mustBe true
      invalidForm.errors.map(_.key) mustBe Seq(expectedKey)
    }
  }

  trait Fixture {

    val validFormData = Data(
      sex = "Male",
      sexOrientation = "Other", otherSexOrientation = Some("details"),
      ethnicity = None, otherEthnicity = None, preferNotSayEthnicity = Some(true),
      isEnglishFirstLanguage = "Yes"
    )

    val validFormValues = Map(
      "sex" -> "Male",

      "sexOrientation" -> "Other",
      "other_sexOrientation" -> "details",

      "ethnicity" -> "",
      "other_ethnicity" -> "",
      "preferNotSay_ethnicity" -> "true",

      "isEnglishFirstLanguage" -> "Yes"
    )

    val formWrapper = new DiversityQuestionnaireForm().form

    def assertFieldRequired(expectedError: String, formValues: Map[String, String]) =
      assertFormError(expectedError, formValues)

    def assertFieldRequired(expectedError: String, fieldKeysToClear: String*) =
      assertFormError(expectedError, validFormValues ++ fieldKeysToClear.map(k => k -> ""))

    def assertFormError(expectedKey: String, invalidFormValues: Map[String, String]) = {
      val invalidForm = formWrapper.bind(invalidFormValues)
      invalidForm.hasErrors mustBe true
      invalidForm.errors.map(_.key) mustBe Seq(expectedKey)
    }
  }
}
