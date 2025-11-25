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

class OnboardQuestionsFormSpec extends BaseFormSpec {

  "Onboard questions form form" should {
    "be valid for a valid NI number" in new TestFixture {
      val form = onboardQuestionsForm.bind(Map("niNumber" -> "NR292055B"))
      form.hasErrors mustBe false
      form.hasGlobalErrors mustBe false
    }

    "be invalid for invalid NI numbers" in new TestFixture {
      val invalidFormats = Seq(
        "123456789", // All numbers
        "NR292055",  // Last character not in A, B, C, D - missing
        "NR292055E", // Last character not in A, B, C, D
        "NR2920555", // Last character is a number
        "NRZ92055A", // 1st digit is a letter
        "NR2Z2055A", // 2nd digit is a letter
        "NR29Z055A", // 3nd digit is a letter
        "NR292Z55A", // 4th digit is a letter
        "NR2920Z5A", // 5th digit is a letter
        "NR29205ZA", // 6th digit is a letter
        "GB292055A", // illegal combination for 1st 2 letters - GB
        "BG292055A", // illegal combination for 1st 2 letters - BG
        "NK292055A", // illegal combination for 1st 2 letters - NK
        "KN292055A", // illegal combination for 1st 2 letters - KN
        "TN292055A", // illegal combination for 1st 2 letters - TN
        "NT292055A", // illegal combination for 1st 2 letters - NT
        "ZZ292055A", // illegal combination for 1st 2 letters - ZZ
        "DR292055A", // first character must not be D,F,I,Q,U or V.
        "FR292055A", // first character must not be D,F,I,Q,U or V.
        "IR292055A", // first character must not be D,F,I,Q,U or V.
        "QR292055A", // first character must not be D,F,I,Q,U or V.
        "UR292055A", // first character must not be D,F,I,Q,U or V.
        "VR292055A", // first character must not be D,F,I,Q,U or V.
        "ND292055A", // second character must not be D, F, I, O, Q, U or V.
        "NF292055A", // second character must not be D, F, I, O, Q, U or V.
        "NI292055A", // second character must not be D, F, I, O, Q, U or V.
        "NO292055A", // second character must not be D, F, I, O, Q, U or V.
        "NQ292055A", // second character must not be D, F, I, O, Q, U or V.
        "NU292055A", // second character must not be D, F, I, O, Q, U or V.
        "NV292055A", // second character must not be D, F, I, O, Q, U or V.
        "ZZ12Z456Z", // multiple failures
        "NR29",      // not enough characters
        "?R123456A", // special character in character 1
        "N?123456A", // special character in character 2
        "NR?23456A", // special character in character 3
        "NR1?3456A", // special character in character 4
        "NR12?456A", // special character in character 5
        "NR123?56A", // special character in character 6
        "NR1234?6A", // special character in character 7
        "NR12345?A", // special character in character 8
        "NR123456?", // special character in character 9
        "NR123456 "  // space at the end
      )

      invalidFormats.foreach { niNumber =>
        val form = onboardQuestionsForm.bind(Map("niNumber" -> niNumber))
        form.hasErrors mustBe true
        form.hasGlobalErrors mustBe false
        val errors = form.errors.map(_.message)
        errors mustBe List("error.niNumber.wrong.format")
      }
    }

    "be invalid for length as well as format" in new TestFixture {
      val form = onboardQuestionsForm.bind(Map("niNumber" -> "NR292055AA"))
      form.hasErrors mustBe true
      form.hasGlobalErrors mustBe false
      val errors = form.errors.map(_.message)
      errors mustBe List("error.maxLength", "error.niNumber.wrong.format")
    }
  }

  trait TestFixture {
    val onboardQuestionsForm = new OnboardQuestionsForm().form
  }
}
