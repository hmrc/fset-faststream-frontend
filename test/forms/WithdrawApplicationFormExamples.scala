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

object WithdrawApplicationFormExamples {
  val ValidForm: WithdrawApplicationForm.Data = WithdrawApplicationForm.Data(
    wantToWithdraw = "Yes", reason = Some("found another job"), otherReason = None
  )

  val OtherReasonValidForm: WithdrawApplicationForm.Data =
    WithdrawApplicationForm.Data(
      wantToWithdraw = "Yes", reason = Some("Other (provide details)"), otherReason = Some("more info")
    )

  val OtherReasonInvalidNoReasonForm: WithdrawApplicationForm.Data =
    WithdrawApplicationForm.Data(wantToWithdraw = "Yes", reason = None, otherReason = None)

  val OtherReasonInvalidNoOtherReasonMoreInfoForm: WithdrawApplicationForm.Data =
    WithdrawApplicationForm.Data(wantToWithdraw = "Yes", reason = Some("Other (provide details)"), otherReason = None)

  val ValidMap: Map[String, String] = Map[String, String](
    "wantToWithdraw" -> "Yes",
    "reason" -> "found another job")

  val OtherReasonValidMap: Map[String, String] = Map[String, String](
    "wantToWithdraw" -> "Yes",
    "reason" -> "Other (provide details)",
    "otherReason" -> "more info"
  )

  // Even though the other reason hasn't been provided, it will pass validation because "wantToWithdraw" is "No"
  val NoOtherReasonProvidedButShouldPassValidationMap: Map[String, String] = Map[String, String](
    "wantToWithdraw" -> "No",
    "reason" -> "Other (provide details)",
    "otherReason" -> ""
  )

  val OtherReasonInvalidNoReasonMap: Map[String, String] = Map[String, String](
    "wantToWithdraw" -> "Yes")

  val OtherReasonInvalidNoOtherReasonMoreInfoMap: Map[String, String] = Map[String, String](
    "wantToWithdraw" -> "Yes",
    "reason" -> "Other (provide details)")

  val ValidFormUrlEncodedBody: Seq[(String, String)] = Seq(
    "wantToWithdraw" -> "Yes",
    "reason" -> "found another job")

  val OtherReasonValidFormUrlEncodedBody: Seq[(String, String)] = Seq(
    "wantToWithdraw" -> "Yes",
    "reason" -> "Other (provide details)",
    "otherReason" -> "more info"
  )

  val OtherReasonInvalidNoReasonFormUrlEncodedBody: Seq[(String, String)] = Seq(
    "wantToWithdraw" -> "Yes")

  val OtherReasonInvalidNoOtherReasonMoreInfoFormUrlEncodedBody: Seq[(String, String)] = Seq(
    "wantToWithdraw" -> "Yes",
    "reason" -> "Other (provide details)")
}
