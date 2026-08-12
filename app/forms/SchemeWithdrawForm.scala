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

import javax.inject.Singleton
import mappings.Mappings.*
import play.api.data.{Form, FormError}
import play.api.data.Forms.*
import play.api.data.format.Formatter
import play.api.i18n.Messages

@Singleton
class SchemeWithdrawForm {

  def form(candidatesSchemes: Seq[SchemeId])(implicit messages: Messages): Form[SchemeWithdrawForm.Data] = {
    Form(mapping(
      "wantToWithdraw" -> nonEmptyTrimmedText("error.wantToWithdraw.required", 5),
      "scheme" -> of(schemeFormatter(candidatesSchemes)),
      "reason" -> text
    )(SchemeWithdrawForm.Data.apply)(f => Some(Tuple.fromProductTyped(f))))
  }

  def schemeFormatter(candidatesSchemes: Seq[SchemeId])(implicit messages: Messages): Formatter[String] = new Formatter[String] {
    override def bind(key: String, request: Map[String, String]): Either[Seq[FormError], String] = {

      val dependencyCheck = request.isWantToWithdrawSelected
      val isValid = request.isPostedSchemeValid(candidatesSchemes)

      (dependencyCheck, isValid) match {
        case (true, true) => Right(request.schemeParam)
        case (true, false) => Left(List(FormError(key, "Choose a scheme to withdraw")))
        case (false, _) => Right("")
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }
}

implicit class RequestValidation(request: Map[String, String]) {
  def param(name: String): Option[String] = request.collectFirst { case (key, value) if key == name => value }

  private def wantToWithdrawParam: String = param("wantToWithdraw").getOrElse("")

  def isWantToWithdrawSelected: Boolean = wantToWithdrawParam == "Yes"

  def schemeParam: String = param("scheme").getOrElse("")

  def isPostedSchemeValid(candidatesSchemes: Seq[SchemeId]): Boolean = candidatesSchemes.contains(SchemeId(schemeParam))
}

object SchemeWithdrawForm {
  case class Data(
                   wantToWithdraw: String,
                   scheme: String,
                   reason: String
                 )
}
