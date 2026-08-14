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

import mappings.Mappings.*
import play.api.data.Forms.*
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.i18n.Messages

import javax.inject.Singleton

@Singleton
class WithdrawApplicationForm {

  private def otherReasonFormatter(maxLength: Int)(implicit messages: Messages) = new Formatter[Option[String]] {
    override def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {

      val dependencyCheck = request.isWantToWithdrawSelected && request.isOtherReasonSelected
      val isFilled = request.isOtherReasonFilled
      val isCorrectSize = request.isOtherReasonSizeValid(maxLength)

      (dependencyCheck, isFilled, isCorrectSize) match {
        case (true, true, true) => Right(Some(request.otherReasonParam))
        case (true, true, false) => Left(List(FormError(key, Messages(s"error.$key.maxLength"))))
        case (true, false, false) => Left(List(FormError(key, Messages("error.required.reason.more_info"))))
        case (true, false, _) => Right(None)
        case (false, _, _) => Right(None)
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

  def form(implicit messages: Messages): Form[WithdrawApplicationForm.Data] = Form(
    mapping(
      "wantToWithdraw" -> nonEmptyTrimmedText("error.wantToWithdraw.required", 5),
      "reason" -> of(requiredFormatterWithMaxLengthCheck("wantToWithdraw", "reason", Some(64))),
      "otherReason" -> of(otherReasonFormatter(300))
    )(WithdrawApplicationForm.Data.apply)(f => Some(Tuple.fromProductTyped(f)))
  )

  implicit class RequestValidation(request: Map[String, String]) {
    def param(name: String): Option[String] = request.collectFirst { case (key, value) if key == name => value }

    private def wantToWithdrawParam: String = param("wantToWithdraw").getOrElse("")

    def isWantToWithdrawSelected: Boolean = wantToWithdrawParam == "Yes"

    private def reasonParam = param("reason")

    def otherReasonParam: String = param("otherReason").getOrElse("")

    def isOtherReasonSelected: Boolean = isWantToWithdrawSelected && reasonParam.contains("Other (provide details)")

    def isOtherReasonFilled: Boolean = otherReasonParam.nonEmpty

    // If otherReason is selected then the description must not be empty and not exceed the max size
    def isOtherReasonSizeValid(max: Int): Boolean = isOtherReasonSelected &&
      otherReasonParam.nonEmpty && otherReasonParam.length <= max
  }
}

object WithdrawApplicationForm {
  case class Data(
                   wantToWithdraw: String,
                   reason: Option[String],
                   otherReason: Option[String]
                 )
}
