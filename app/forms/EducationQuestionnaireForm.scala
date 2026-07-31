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

import connectors.exchange._
import forms.EducationQuestionnaireForm.{PostgradUniversity, TextMaxSize}

import javax.inject.Singleton
import mappings.Mappings._
import mappings.PostCodeMapping._
import models.view.questionnaire.{DegreeTypes, Universities, UniversityDegreeCategories}
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.i18n.Messages

@Singleton
class EducationQuestionnaireForm {
  // TODO: the universityQuestionKey looks to now be redundant
  def form(isFsOrSdipFs: Boolean, universityQuestionKey: String)(implicit messages: Messages): Form[EducationQuestionnaireForm.Data] = Form(
    mapping(
      "liveInUKBetween14and18" -> nonEmptyTrimmedText("error.liveInUKBetween14and18.required", 31),
      "postcodeQ" -> of(requiredFormatterWithValidationCheckAndSeparatePreferNotToSay("liveInUKBetween14and18",
        "postcodeQ", "preferNotSay_postcodeQ", Some(TextMaxSize))
      (messages, postCode => !postcodePattern.pattern.matcher(postCode).matches(), "error.postcodeQ.invalid")),
      "preferNotSay_postcodeQ" -> optional(checked(Messages("error.required.postcodeQ"))),
      "schoolType14to16" -> of(requiredFormatterWithMaxLengthCheck("liveInUKBetween14and18", Some(TextMaxSize))),
      "freeSchoolMeals" -> of(requiredFormatterWithMaxLengthCheck("liveInUKBetween14and18", Some(TextMaxSize))),
      "isCandidateCivilServant" -> nonEmptyTrimmedText("error.isCandidateCivilServant.required", 31),
      "hasDegree" -> of(requiredFormatterWithMaxLengthCheck("isCandidateCivilServant", Some(3))),
      "university" -> of(universityFormatter("hasDegree")),
      "universityDegreeCategory" -> of(universityDegreeCategoryFormatter("hasDegree")),
      "degreeType" -> of(degreeTypeFormatter("hasDegree", "degreeType")),

      // If the candidate hasDegree then this must also be answered if the candidate is Faststream or SdipFaststream
      "hasPostgradDegree" -> of(requiredFormatterWithMaxLengthCheck(isFsOrSdipFs, "hasDegree", Some(3))),
      "postgradUniversity" -> mapping(
        "university" -> of(universityFormatter("hasPostgradDegree")),
        "degreeCategory" -> of(universityDegreeCategoryFormatter("hasPostgradDegree")),
        "degreeType" -> of(degreeTypeFormatter("hasPostgradDegree", "postgradUniversity.degreeType"))
      )(PostgradUniversity.apply)(f => Some(Tuple.fromProductTyped(f)))
    )(EducationQuestionnaireForm.Data.apply)(f => Some(Tuple.fromProductTyped(f)))
  )

  private def optionalParamToMap[T](key: String, optValue: Option[T]) = {
    optValue match {
      case None => Map.empty[String, String]
      case Some(value) => Map(key -> value.toString)
    }
  }

  private def universityFormatter(requiredKey: String)(implicit messages: Messages) = new Formatter[Option[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val requiredField: Option[String] = if (request.isEmpty) None else request.get(requiredKey)
      val keyField: Option[String] = if (request.isEmpty) None else request.get(key).map(_.trim)
      val requiredErrorMsg = messages(s"error.$key.required")

      (requiredField, keyField) match {
        case (Some("Yes"), None) => Left(List(FormError(key, requiredErrorMsg)))
        case (Some("Yes"), Some(keyValue)) if keyValue.trim.isEmpty => Left(List(FormError(key, requiredErrorMsg)))
        case (Some("Yes"), Some(_)) if !request.isUniversityValid(key) => Left(List(FormError(key, messages(s"error.$key.invalid"))))
        case _ => Right(keyField)
      }
    }

    def unbind(key: String, value: Option[String]): Map[String, String] = optionalParamToMap(key, value)
  }

  private def universityDegreeCategoryFormatter(requiredKey: String)(implicit messages: Messages) = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val requiredField: Option[String] = if (data.isEmpty) None else data.get(requiredKey)
      val keyField: Option[String] = if (data.isEmpty) None else data.get(key).map(_.trim)
      val requiredErrorMsg = messages(s"error.$key.required")

      (requiredField, keyField) match {
        case (Some("Yes"), None) => Left(List(FormError(key, requiredErrorMsg)))
        case (Some("Yes"), Some(keyValue)) if keyValue.trim.isEmpty => Left(List(FormError(key, requiredErrorMsg)))
        case (Some("Yes"), Some(_)) if !data.isDegreeCategoryValid(key) => Left(List(FormError(key, messages(s"error.$key.invalid"))))
        case _ => Right(keyField)
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = optionalParamToMap(key, value)
  }

  private def degreeTypeFormatter(requiredKey: String, errorKey: String)(implicit messages: Messages) = new Formatter[Option[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val requiredField: Option[String] = if (request.isEmpty) None else request.get(requiredKey)
      val keyField: Option[String] = if (request.isEmpty) None else request.get(key).map(_.trim)
      val requiredErrorMsg = messages(s"error.$errorKey.required")

      (requiredField, keyField) match {
        case (Some("Yes"), None) => Left(List(FormError(key, requiredErrorMsg)))
        case (Some("Yes"), Some(keyValue)) if keyValue.trim.isEmpty => Left(List(FormError(key, requiredErrorMsg)))
        case (Some("Yes"), Some(_)) if !request.isDegreeTypeValid(key) => Left(List(FormError(key, messages(s"error.$errorKey.invalid"))))
        case _ => Right(keyField)
      }
    }

    def unbind(key: String, value: Option[String]): Map[String, String] = optionalParamToMap(key, value)
  }

  implicit class RequestValidation(request: Map[String, String]) {
    def param(name: String): Option[PostCode] = request.collectFirst { case (key, value) if key == name => value }

    def isDegreeTypeValid(key: String): Boolean = DegreeTypes.validDegreeTypes.contains(param(key).getOrElse(""))

    def isUniversityValid(key: String): Boolean = Universities.validUniversities.contains(param(key).getOrElse(""))

    def isDegreeOrPostgradDegreeSelected(degreeKey: String): Boolean = param(degreeKey).getOrElse("").contains("Yes")

    def otherDegreeTypeParam(key: String): PostCode = param(key).getOrElse("")

    // key is either universityDegreeCategory or postgradUniversity.degreeCategory
    def isDegreeCategoryValid(key: String): Boolean = UniversityDegreeCategories.validDegreeCategories.contains(param(key).getOrElse(""))
  }
}

object EducationQuestionnaireForm {
  val TextMaxSize = 256
  case class PostgradUniversity(
                                 university: Option[String],
                                 degreeCategory: Option[String],
                                 degreeType: Option[String]
                               )
  object  PostgradUniversity {
    val empty: PostgradUniversity = PostgradUniversity(university = None, degreeCategory = None, degreeType = None)
  }
  case class Data(
                   liveInUKBetween14and18: String,
                   postcode: Option[String],
                   preferNotSayPostcode: Option[Boolean],
                   schoolType14to16: Option[String],
                   freeSchoolMeals: Option[String],
                   isCandidateCivilServant: String,
                   hasDegree: Option[String],
                   university: Option[String],
                   universityDegreeCategory: Option[String],
                   degreeType: Option[String],
                   hasPostgradDegree: Option[String],
                   postgradUniversity: PostgradUniversity
  ) {

    //scalastyle:off method.length
    def toExchange(isFsOrSdipFs: Boolean)(implicit messages: Messages): Questionnaire = {
      def getAnswer(field: Option[String], preferNotToSayField: Option[Boolean], otherDetails: Option[String] = None) = {
        preferNotToSayField match {
          case Some(true) => Answer(answer = None, otherDetails, unknown = Some(true))
          case _ => Answer(field, otherDetails, unknown = None)
        }
      }

      val freeSchoolMealAnswer = freeSchoolMeals match {
        case None | Some("I don't know/prefer not to say") => Answer(answer = None, otherDetails = None, unknown = Some(true))
        case _ => Answer(freeSchoolMeals, otherDetails = None, unknown = None)
      }

      def getOptionalSchoolList(implicit messages: Messages) = {
        if (liveInUKBetween14and18 == "Yes") {
          List(
            Question(Messages("postcode.question"), getAnswer(postcode, preferNotSayPostcode)),
            Question(Messages("schoolType14to16.question"), Answer(schoolType14to16, otherDetails = None, unknown = None)),
            Question(Messages("freeSchoolMeals.question"), freeSchoolMealAnswer))
        } else {
          List.empty
        }
      }

      def getOptionalUniversityList(implicit messages: Messages): List[Question] = {
        val degreeQuestions = hasDegree match {
          case Some("Yes") =>
            val postgradDegreeQuestion = Question(
              Messages("hasPostgradDegree.question"), getAnswer(hasPostgradDegree, preferNotToSayField = None, otherDetails = None)
            )
            val degreeQuestions = List(
              Question(Messages("university.question"), getAnswer(university, preferNotToSayField = None)),
              Question(Messages("universityDegreeCategory.question"), getAnswer(
                universityDegreeCategory,
                preferNotToSayField = None)
              ),
              Question(Messages("degreeType.question"), getAnswer(degreeType, preferNotToSayField = None))
            )
            if (isFsOrSdipFs) {
              // The postgrad degree question is only relevant for faststream or sdipFaststream candidates
              degreeQuestions ++ List(postgradDegreeQuestion)
            } else {
              degreeQuestions
            }
          case _ => List.empty
        }

        val postgradDegreeQuestions = hasPostgradDegree match {
          case Some("Yes") => List(
            Question(
              Messages("postgradDegree.university.question"),
              getAnswer(postgradUniversity.university, preferNotToSayField = None, otherDetails = None)
            ),
            Question(
              Messages("postgradDegree.degreeCategory.question"),
              getAnswer(postgradUniversity.degreeCategory, preferNotToSayField = None, otherDetails = None)
            ),
            Question(
              Messages("postgradDegree.degreeType.question"),
              getAnswer(postgradUniversity.degreeType, preferNotToSayField = None)
            )
          )
          case _ => List.empty
        }
        degreeQuestions ++ postgradDegreeQuestions
      }

      Questionnaire(
        List(Question(Messages("liveInUKBetween14and18.question"), Answer(Some(liveInUKBetween14and18), otherDetails = None, unknown = None))) ++
          getOptionalSchoolList ++
          List(Question(Messages("hasDegree.question"), getAnswer(hasDegree, preferNotToSayField = None))) ++
          getOptionalUniversityList
      )
    }

    /**
     * It makes sure that when you select "No" as an answer to "live in the UK between 14 and 18" question, the dependent
     * questions are reset to None.
     *
     * This is a kind of backend partial clearing form functionality.
     */
    def sanitizeData: Data = {
      sanitizeLiveInUK.sanitizeUniversity
    }

    private def sanitizeLiveInUK = {
      if (liveInUKBetween14and18 == "Yes") {
        this.copy(
          postcode = sanitizeValueWithPreferNotToSay(postcode, preferNotSayPostcode),
          schoolType14to16 = schoolType14to16
        )
      } else {
        this.copy(
          postcode = None,
          preferNotSayPostcode = None,
          schoolType14to16 = None,
          freeSchoolMeals = None)
      }
    }

    private def sanitizeUniversity = {
      if (hasDegree.contains("No")) {
        this.copy(
          university = None,
          universityDegreeCategory = None,
          degreeType = None
        )
      } else {
        this
      }
    }

    private def sanitizeValueWithPreferNotToSay(value: Option[String], preferNotToSayValue: Option[Boolean]): Option[String] = {
      preferNotToSayValue match {
        case Some(true) => None
        case _ => value
      }
    }
  }
}
