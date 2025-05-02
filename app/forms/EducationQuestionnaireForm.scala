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
  def form(isFsOrSdipFs: Boolean, universityQuestionKey: String)(implicit messages: Messages) = Form(
    mapping(
      "liveInUKBetween14and18" -> nonEmptyTrimmedText("error.liveInUKBetween14and18.required", 31),
      "postcodeQ" -> of(requiredFormatterWithValidationCheckAndSeparatePreferNotToSay("liveInUKBetween14and18",
        "postcodeQ", "preferNotSay_postcodeQ", Some(TextMaxSize))
      (messages, postCode => !postcodePattern.pattern.matcher(postCode).matches(), "error.postcodeQ.invalid")),
      "preferNotSay_postcodeQ" -> optional(checked(Messages("error.required.postcodeQ"))),
      "schoolName14to16" -> of(requiredFormatterWithValidationCheckAndSeparatePreferNotToSay("liveInUKBetween14and18",
        "schoolName14to16", "preferNotSay_schoolName14to16", Some(TextMaxSize))),
      "schoolId14to16" -> of(schoolIdFormatter("schoolName14to16")),
      "preferNotSay_schoolName14to16" -> optional(checked(Messages("error.required.schoolName14to16"))),
      "schoolType14to16" -> of(requiredFormatterWithMaxLengthCheck("liveInUKBetween14and18", Some(TextMaxSize))),
      "schoolName16to18" -> of(requiredFormatterWithValidationCheckAndSeparatePreferNotToSay("liveInUKBetween14and18",
        "schoolName16to18", "preferNotSay_schoolName16to18", Some(TextMaxSize))),
      "schoolId16to18" -> of(schoolIdFormatter("schoolName16to18")),
      "preferNotSay_schoolName16to18" -> optional(checked(Messages("error.required.schoolName16to18"))),
      "freeSchoolMeals" -> of(requiredFormatterWithMaxLengthCheck("liveInUKBetween14and18", Some(TextMaxSize))),
      "isCandidateCivilServant" -> nonEmptyTrimmedText("error.isCandidateCivilServant.required", 31),
      "hasDegree" -> of(requiredFormatterWithMaxLengthCheck("isCandidateCivilServant", Some(3))),
      "university" -> of(universityFormatter("hasDegree")),
      "universityDegreeCategory" -> of(universityDegreeCategoryFormatter("hasDegree")),
      "degreeType" -> of(degreeTypeFormatter("hasDegree", "degreeType")),
      "otherDegreeType" -> of(otherDegreeTypeFormatter("hasDegree", "degreeType", "otherDegreeType", TextMaxSize)),

      // If the candidate hasDegree then this must also be answered if the candidate is Faststream or SdipFaststream
      "hasPostgradDegree" -> of(requiredFormatterWithMaxLengthCheck(isFsOrSdipFs, "hasDegree", Some(3))),
      "postgradUniversity" -> mapping(
        "university" -> of(universityFormatter("hasPostgradDegree")),
        "degreeCategory" -> of(universityDegreeCategoryFormatter("hasPostgradDegree")),
        "degreeType" -> of(degreeTypeFormatter("hasPostgradDegree", "postgradUniversity.degreeType")),
        "otherDegreeType" -> of(otherDegreeTypeFormatter(
          "hasPostgradDegree", "postgradUniversity.degreeType", "postgradUniversity.otherDegreeType", TextMaxSize))
      )(PostgradUniversity.apply)(f => Some(Tuple.fromProductTyped(f)))
    )(EducationQuestionnaireForm.Data.apply)(f => Some(Tuple.fromProductTyped(f)))
  )

  def schoolIdFormatter(schoolNameKey: String) = new Formatter[Option[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val schoolId = request.getOrElse(key, "")
      val schoolName = request.getOrElse(schoolNameKey, "")
      (schoolName.trim.nonEmpty, schoolId.trim.nonEmpty) match {
        case (true, true) => Right(Some(schoolId))
        case _ => Right(None)
      }
    }

    def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

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

  def universityDegreeCategoryFormatter(requiredKey: String)(implicit messages: Messages) = new Formatter[Option[String]] {
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

  // requiredKey: "hasDegree" or "hasPostgradDegree"
  // degreeTypeKey: "degreeType" or "postgradUniversity.degreeType"
  // otherDegreeTypeKey: "otherDegreeType" or "postgradUniversity.otherDegreeType"
  private def otherDegreeTypeFormatter(requiredKey: String, degreeTypeKey: String, otherDegreeTypeKey: String, maxSize: Int)(
    implicit messages: Messages) = new Formatter[Option[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val isDegreeSelected = request.isDegreeOrPostgradDegreeSelected(requiredKey)
      val dependencyCheck = request.isOtherSelected(degreeTypeKey: String)

      val isFilled = request.isOtherFilled(otherDegreeTypeKey)
      val isCorrectSize = request.isOtherSizeValid(otherDegreeTypeKey, maxSize)

      (isDegreeSelected && dependencyCheck, isFilled, isCorrectSize) match {
        case (true, false, _) => Left(List(FormError(key, messages(s"error.$otherDegreeTypeKey.required"))))
        case (true, true, false) => Left(List(FormError(key, messages(s"error.$otherDegreeTypeKey.maxLength", maxSize))))
        case (true, true, true) => Right(Some(request.otherDegreeTypeParam(otherDegreeTypeKey)))
        case (false, _, _) => Right(None)
      }
    }

    def unbind(key: String, value: Option[String]): Map[String, String] = optionalParamToMap(key, value)
  }

  implicit class RequestValidation(request: Map[String, String]) {
    def param(name: String) = request.collectFirst { case (key, value) if key == name => value }

    def isDegreeTypeValid(key: String) = DegreeTypes.validDegreeTypes.contains(param(key).getOrElse(""))

    def isUniversityValid(key: String) = Universities.validUniversities.contains(param(key).getOrElse(""))

    // degreeKey: "hasDegree" or "hasPostgradDegree"
    def isDegreeOrPostgradDegreeSelected(degreeKey: String) = param(degreeKey).getOrElse("").contains("Yes")

    // Checks if "Other" is selected for either degree or postgradDegree
    // degreeTypeKey is either "degreeType" or "postgradUniversity.degreeType"
    def isOtherSelected(degreeTypeKey: String) = param(degreeTypeKey).getOrElse("").contains("Other")

    def isOtherFilled(otherKey: String) = param(otherKey).getOrElse("").nonEmpty

    def isOtherSizeValid(otherKey: String, max: Int) = isOtherFilled(otherKey) && param(otherKey).getOrElse("").length < max

    def otherDegreeTypeParam(key: String) = param(key).getOrElse("")

    // key is either universityDegreeCategory or postgradUniversity.degreeCategory
    def isDegreeCategoryValid(key: String) = UniversityDegreeCategories.validDegreeCategories.contains(param(key).getOrElse(""))
  }
}

object EducationQuestionnaireForm {
  val TextMaxSize = 256
  case class PostgradUniversity(
                                 university: Option[String],
                                 degreeCategory: Option[String],
                                 degreeType: Option[String],
                                 otherDegreeType: Option[String]
                               )
  object  PostgradUniversity {
    val empty = PostgradUniversity(university = None, degreeCategory = None, degreeType = None, otherDegreeType = None)
  }
  case class Data(
                   liveInUKBetween14and18: String,
                   postcode: Option[String],
                   preferNotSayPostcode: Option[Boolean],
                   schoolName14to16: Option[String],
                   schoolId14to16: Option[String],
                   preferNotSaySchoolName14to16: Option[Boolean],
                   schoolType14to16: Option[String],
                   schoolName16to18: Option[String],
                   schoolId16to18: Option[String],
                   preferNotSaySchoolName16to18: Option[Boolean],
                   freeSchoolMeals: Option[String],
                   isCandidateCivilServant: String,
                   hasDegree: Option[String],
                   university: Option[String],
                   universityDegreeCategory: Option[String],
                   degreeType: Option[String],
                   otherDegreeType: Option[String],
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
            Question(Messages("schoolName14to16.question"), getAnswer(schoolName14to16, preferNotSaySchoolName14to16, schoolId14to16)),
            Question(Messages("schoolType14to16.question"), Answer(schoolType14to16, otherDetails = None, unknown = None)),
            Question(Messages("schoolName16to18.question"), getAnswer(schoolName16to18, preferNotSaySchoolName16to18, schoolId16to18)),
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
              Question(Messages("degreeType.question"), getAnswer(degreeType, preferNotToSayField = None, otherDetails = otherDegreeType))
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
              getAnswer(postgradUniversity.degreeType, preferNotToSayField = None, otherDetails = postgradUniversity.otherDegreeType)
            )
          )
          case _ => List.empty
        }
        degreeQuestions ++ postgradDegreeQuestions
      }

      Questionnaire(
        List(Question(Messages("liveInUKBetween14and18.question"), Answer(Some(liveInUKBetween14and18), None, None))) ++
          getOptionalSchoolList ++
          List(Question(Messages("hasDegree.question"), getAnswer(hasDegree, None))) ++
          getOptionalUniversityList
      )
    } //scalastyle:on

    /**
     * It makes sure that when you select "No" as an answer to "live in the UK between 14 and 18" question, the dependent
     * questions are reset to None.
     *
     * This is a kind of backend partial clearing form functionality.
     */
    def sanitizeData = {
      sanitizeLiveInUK.sanitizeUniversity
    }

    private def sanitizeLiveInUK = {
      if (liveInUKBetween14and18 == "Yes") {
        this.copy(
          postcode = sanitizeValueWithPreferNotToSay(postcode, preferNotSayPostcode),
          schoolName14to16 = sanitizeValueWithPreferNotToSay(schoolName14to16, preferNotSaySchoolName14to16),
          schoolType14to16 = schoolType14to16,
          schoolName16to18 = sanitizeValueWithPreferNotToSay(schoolName16to18, preferNotSaySchoolName16to18)
        )
      } else {
        this.copy(
          postcode = None,
          preferNotSayPostcode = None,
          schoolName14to16 = None,
          schoolType14to16 = None,
          schoolName16to18 = None,
          preferNotSaySchoolName16to18 = None,
          freeSchoolMeals = None)
      }
    }

    private def sanitizeUniversity = {
      if (hasDegree.contains("No")) {
        this.copy(
          university = None,
          universityDegreeCategory = None,
          degreeType = None,
          otherDegreeType = None
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
