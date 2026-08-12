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

import connectors.exchange.referencedata.SchemeId
import play.api.data.Form

class SchemeWithdrawFormSpec extends BaseFormSpec {

  val candidatesSchemes: Seq[SchemeId] = Seq(SchemeId("Commercial"), SchemeId("Finance"))
  def withdrawForm: Form[SchemeWithdrawForm.Data] = new SchemeWithdrawForm().form(candidatesSchemes)

  "Scheme withdraw form" should {
    "be valid when withdrawing a scheme the candidate is in the running for" in {
      val form = withdrawForm.bind(Map("wantToWithdraw" -> "Yes", "scheme" -> "Commercial", "reason" -> ""))
      form.hasErrors mustBe false
    }

    "be invalid when not submitting any data" in {
      val form = withdrawForm.bind(Map.empty[String, String])
      form.hasErrors mustBe true
      form.errors.flatMap(_.messages) mustBe List("error.wantToWithdraw.required", "error.required")
    }

    "be invalid when withdrawing a scheme the candidate is not in the running for" in {
      val form = withdrawForm.bind(Map("wantToWithdraw" -> "Yes", "scheme" -> "BOOM", "reason" -> "my reason"))
      form.hasErrors mustBe true
      form.errors.flatMap(_.messages) mustBe List("Choose a scheme to withdraw")
    }

    "be invalid when withdrawing a scheme and a reason is not posted" in {
      val form = withdrawForm.bind(Map("wantToWithdraw" -> "Yes", "scheme" -> "Commercial"))
      form.hasErrors mustBe true
      form.errors.flatMap(_.messages) mustBe List("error.required")
    }
  }
}
