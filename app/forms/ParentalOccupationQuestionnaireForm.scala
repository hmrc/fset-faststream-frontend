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

import connectors.exchange.{Answer, Question, Questionnaire}

import javax.inject.Singleton
import models.view.{SimpleAnswerOptions, ValidAnswers}
import models.view.questionnaire.{Employees, Occupations, OrganizationSizes, ParentQualifications}
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.i18n.Messages

@Singleton
class ParentalOccupationQuestionnaireForm {
  private def requiredValidValuesFormatter(errorKey: String, validAnswers: ValidAnswers)(implicit messages: Messages) = new Formatter[String] {
    override def bind(key: String, request: Map[String, String]): Either[Seq[FormError], String] = {
      val requiredErrorMsg = messages(s"error.required.$errorKey")
      val invalidErrorMsg = messages(s"error.$errorKey.invalid")

      request.getOrElse(key, "").trim match {
        case param if param.isEmpty => Left(List(FormError(key, requiredErrorMsg)))
        case param if !validAnswers.values.contains(param) => Left(List(FormError(key, invalidErrorMsg)))
        case param => Right(param)
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }

  def employedDependentFormatter(validAnswers: ValidAnswers)(implicit messages: Messages) = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val check = data.get("employedParent")
      val value = data.get(key).filterNot(_.isEmpty)

      val invalidErrorMsg = messages (s"error.$key.invalid")

      (check, value) match {
        case (Some("Employed"), Some(param)) if !validAnswers.values.contains(param) => Left(List(FormError(key, invalidErrorMsg)))
        case (Some("Employed"), Some(_)) => Right(value)
        case (Some("Employed"), None) => Left(List(FormError(key, Messages(s"error.required.$key"))))
        case _ => Right(None)
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

  def form(implicit messages: Messages) = Form(
    mapping(
      "socioEconomicBackground" -> of(requiredValidValuesFormatter("socioEconomicBackground", SimpleAnswerOptions)),
      "parentsDegree" -> of(requiredValidValuesFormatter("parentsDegree", ParentQualifications)),
      "employedParent" -> of(requiredValidValuesFormatter("employmentStatus", ParentEmploymentAnswers)),
      "parentsOccupation" -> of(employedDependentFormatter(Occupations)),
      "employee" -> of(employedDependentFormatter(Employees)),
      "organizationSize" -> of(employedDependentFormatter(OrganizationSizes)),
      "supervise" -> of(employedDependentFormatter(SimpleAnswerOptions))
    )(ParentalOccupationQuestionnaireForm.Data.apply)(ParentalOccupationQuestionnaireForm.Data.unapply)
  )
}

object ParentalOccupationQuestionnaireForm {
  case class Data(
    socioEconomicBackground: String,
    parentsDegree: String,
    employedParent: String,
    parentsOccupation: Option[String],
    employee: Option[String],
    organizationSize: Option[String],
    supervise: Option[String]
  ) {
    def exchange(implicit messages: Messages): Questionnaire = {
      val occupation = if (employedParent == "Employed") parentsOccupation else Some(employedParent)

      Questionnaire(List(
        Question(Messages("socioEconomic.question"), Answer(Some(socioEconomicBackground), otherDetails = None, unknown = None)),
        Question(Messages("parentsDegree.question"), Answer(Some(parentsDegree), otherDetails = None, unknown = None)),
        Question(Messages("parentsOccupation.question"), Answer(occupation.sanitize, otherDetails = None, unknown = None))
      ).concat(
        if (employedParent == "Employed") {
          List(
            Question(Messages("employee.question"), Answer(employee, otherDetails = None, unknown = None)),
            Question(Messages("organizationSize.question"), Answer(organizationSize, otherDetails = None, unknown = None)),
            Question(Messages("supervise.question"), Answer(supervise, otherDetails = None, unknown = None))
          )
        } else {
          List.empty
        }
      ))
    }
  }
}

object ParentEmploymentAnswers extends ValidAnswers {
  val employed = "Employed"
  val unemployedButSeekingWork = "Unemployed but seeking work"
  val longTermUnemployed = "Long term unemployed"
  val retired = "Retired"
  val unknown = "Unknown"
  override val values = List(employed, unemployedButSeekingWork, longTermUnemployed, retired, unknown)
}

