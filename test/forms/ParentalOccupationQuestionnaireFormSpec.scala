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

import forms.ParentalOccupationQuestionnaireForm.Data
import play.api.data.FormError
import play.api.i18n.Messages

class ParentalOccupationQuestionnaireFormSpec extends BaseFormSpec {

  val form = new ParentalOccupationQuestionnaireForm().form

  "the occupation form" should {
    "be valid when all values are correct" in new Fixture {
      val validForm = form.bind(validFormValues)
      val expectedData = validFormData
      val actualData = validForm.get
      actualData mustBe expectedData
    }

    "fail when no socio-economic background" in new Fixture {
      assertFieldRequired(expectedError = "socioEconomicBackground", "socioEconomicBackground")
    }

    "fail when the socioEconomicBackground value posted is invalid" in new Fixture {
      assertFieldInvalid("socioEconomicBackground", "socioEconomicBackground", "BOOM")
    }

    "fail when no parents degree" in new Fixture {
      assertFieldRequired(expectedError = "parentsDegree", "parentsDegree")
    }

    "fail when the parentsDegree value posted is invalid" in new Fixture {
      assertFieldInvalid("parentsDegree", "parentsDegree", "BOOM")
    }

    "fail when no employedParent" in new Fixture {
      assertFieldRequired(expectedError = "employedParent", "employedParent")
    }

    "fail when the employedParent value posted is invalid" in new Fixture {
      assertFieldInvalid("employedParent", "employedParent", "BOOM")
    }

    "fail when no parentsOccupation" in new Fixture {
      assertFieldRequired(expectedError = "parentsOccupation", "parentsOccupation")
    }

    "fail when the parentsOccupation value posted is invalid" in new Fixture {
      assertFieldInvalid("parentsOccupation", "parentsOccupation", "BOOM")
    }

    "fail when no employee" in new Fixture {
      assertFieldRequired(expectedError = "employee", "employee")
    }

    "fail when the employee value posted is invalid" in new Fixture {
      assertFieldInvalid("employee", "employee", "BOOM")
    }

    "fail when no organizationSize" in new Fixture {
      assertFieldRequired(expectedError = "organizationSize", "organizationSize")
    }

    "fail when the organizationSize value posted is invalid" in new Fixture {
      assertFieldInvalid("organizationSize", "organizationSize", "BOOM")
    }

    "fail when no supervise" in new Fixture {
      assertFieldRequired(expectedError = "supervise", "supervise")
    }

    "fail when the supervise value posted is invalid" in new Fixture {
      assertFieldInvalid("supervise", "supervise", "BOOM")
    }

    "be valid when parents were unemployed" in new Fixture {
      val validFormUnemployed = form.bind(validFormValuesUnemployed)
      val expectedData = validFormDataUnemployed
      val actualData = validFormUnemployed.get
      actualData mustBe expectedData
    }

    "transform properly to a question list" in new Fixture {
      val questionList = validFormData.exchange.questions
      questionList.size mustBe 6
      questionList.head.answer.answer mustBe Some("Yes")
      questionList(1).answer.answer mustBe Some("Degree level qualification")
      questionList(2).answer.answer mustBe Some("Traditional professional")
      questionList(3).answer.answer mustBe Some("Employee")
      questionList(4).answer.answer mustBe Some("Small (1 to 24 employees)")
      questionList(5).answer.answer mustBe Some("Yes")
    }

    "transform correctly to a question list when the parent is not employed" in new Fixture {
      val retiredParentFormData = Data(
        socioEconomicBackground = "Yes",
        parentsDegree = "Degree level qualification",
        employedParent = "Retired",
        parentsOccupation = None,
        employee = None,
        organizationSize = None,
        supervise = None
      )

      val questionList = retiredParentFormData.exchange.questions
      questionList.size mustBe 3
      questionList.head.answer.answer mustBe Some("Yes")
      questionList(1).answer.answer mustBe Some("Degree level qualification")
      questionList(2).answer.answer mustBe Some("Retired")
    }
  }

  trait Fixture {
    val validFormData = Data(
      socioEconomicBackground = "Yes",
      parentsDegree =  "Degree level qualification",
      employedParent = "Employed",
      parentsOccupation = Some("Traditional professional"),
      employee = Some("Employee"),
      organizationSize = Some("Small (1 to 24 employees)"),
      supervise = Some("Yes")
    )

    val validFormValues = Map(
      "socioEconomicBackground" -> "Yes",
      "parentsDegree" -> "Degree level qualification",
      "employedParent" -> "Employed",
      "parentsOccupation" -> "Traditional professional",
      "employee" -> "Employee",
      "organizationSize" -> "Small (1 to 24 employees)",
      "supervise" -> "Yes"
    )

    val validFormDataUnemployed = Data(
      socioEconomicBackground = "No",
      parentsDegree = "No formal qualifications",
      employedParent = "Unemployed",
      parentsOccupation = None,
      employee = None,
      organizationSize = None,
      supervise = None
    )

    val validFormValuesUnemployed = Map(
      "socioEconomicBackground" -> "No",
      "parentsDegree" -> "No formal qualifications",
      "employedParent" -> "Unemployed",
      "parentsOccupation" -> "",
      "employee" -> "",
      "organizationSize" -> "",
      "supervise" -> ""
    )

    // This sets the required field to an empty string and verifies the expected error is generated
    def assertFieldRequired(expectedError: String, fieldKeys: String*) =
      assertFormError(expectedError, validFormValues ++ fieldKeys.map(k => k -> ""))

    def assertFieldInvalid(expectedError: String, key: String, value: String) =
      assertFormError(expectedError, validFormValues ++ Map(key -> value))

    def assertFormError(expectedKey: String, invalidFormValues: Map[String, String]) = {
      val invalidForm = form.bind(invalidFormValues)
      invalidForm.hasErrors mustBe true
      invalidForm.errors.map(_.key) mustBe Seq(expectedKey)
    }
  }
}
