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

import forms.WithdrawApplicationForm.Data
import org.scalatest.Assertion
import play.api.data.Form

class WithdrawApplicationFormSpec extends BaseFormSpec {

  "the withdraw application form" should {

    "be valid when the user selects I want to withdraw and provides a reason (no other reason)" in new Fixture {
      val (data, form) = Valid
      form.get mustBe data
    }

    "be valid when the user selects I do not want to withdraw after previously choosing " +
      "'Other (provide details)' and not providing details" in new Fixture {
      val form: Form[Data] = formWrapper.form.bind(WithdrawApplicationFormExamples.NoOtherReasonProvidedButShouldPassValidationMap)
      form.hasErrors mustBe false
    }

    "be valid when the user selects I want to withdraw and provides another reason and more info" in new Fixture {
      val (data, form) = OtherReasonValid
      form.get mustBe data
    }

    "be invalid when the user selects I want to withdraw but provides no reason" in new Fixture {
      assertFormError(Seq(
        "error.reason.required"
      ), WithdrawApplicationFormExamples.OtherReasonInvalidNoReasonMap)
    }

    "be invalid when the user selects I want to withdraw and selects other reason but provides no more info" in new Fixture {
      assertFormError(Seq(
        "error.required.reason.more_info"
      ), WithdrawApplicationFormExamples.OtherReasonInvalidNoOtherReasonMoreInfoMap)
    }
  }

  trait Fixture {

    def formWrapper = new WithdrawApplicationForm

    val Valid: (Data, Form[Data]) = (WithdrawApplicationFormExamples.ValidForm, formWrapper.form.fill(
      WithdrawApplicationFormExamples.ValidForm))

    val OtherReasonValid: (Data, Form[Data]) = (WithdrawApplicationFormExamples.OtherReasonValidForm, formWrapper.form.fill(
      WithdrawApplicationFormExamples.OtherReasonValidForm))

    val OtherReasonInvalidNoReason: (Data, Form[Data]) = (WithdrawApplicationFormExamples.OtherReasonInvalidNoReasonForm, formWrapper.form.fill(
      WithdrawApplicationFormExamples.OtherReasonInvalidNoReasonForm))

    val OtherReasonInvalidNoOtherReasonMoreInfo: (Data, Form[Data]) =
      (WithdrawApplicationFormExamples.OtherReasonInvalidNoOtherReasonMoreInfoForm,
      formWrapper.form.fill(WithdrawApplicationFormExamples.OtherReasonInvalidNoOtherReasonMoreInfoForm))

    def assertFormError(expectedError: Seq[String], invalidFormValues: Map[String, String]): Assertion = {
      val invalidForm: Form[Data] = formWrapper.form.bind(invalidFormValues)
      invalidForm.hasErrors mustBe true
      invalidForm.errors.map(_.message) mustBe expectedError
    }
  }
}
