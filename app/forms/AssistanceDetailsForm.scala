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

import connectors.exchange.AssistanceDetails
import forms.AssistanceDetailsForm.{ disabilityCategoriesList, other, preferNotToSay }
import javax.inject.Singleton
import mappings.Mappings._
import models.ApplicationRoute._
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{ Form, FormError }
import play.api.i18n.Messages

import scala.language.implicitConversions

@Singleton
class AssistanceDetailsForm {
  val isFastStream: Map[String, String] => Boolean = (requestParams: Map[String, String]) =>
    requestParams.getOrElse("applicationRoute", Faststream.toString) == Faststream.toString

  val isSdipFastStream: Map[String, String] => Boolean = (requestParams: Map[String, String]) =>
    requestParams.getOrElse("applicationRoute", SdipFaststream.toString) == SdipFaststream.toString

  val isFastStreamOrSdipFastStream: Map[String, String] => Boolean = (requestParams: Map[String, String]) =>
    isFastStream(requestParams) || isSdipFastStream(requestParams)

  val isEdipOrSdip: Map[String, String] => Boolean = (requestParams: Map[String, String]) => {
    requestParams.getOrElse("applicationRoute", Faststream.toString) == Edip.toString ||
      requestParams.getOrElse("applicationRoute", Faststream.toString) == Sdip.toString
  }

  val otherDisabilityCategoryMaxSize = 2048
  def form(implicit messages: Messages) = Form(
    mapping(
      "hasDisability" -> of(hasDisabilityFormatter),
      "disabilityImpact" -> of(disabilityImpactFormatter),
      "disabilityCategories" -> of(disabilityCategoriesFormatter),
      "otherDisabilityDescription" -> of(otherDisabilityDescriptionFormatter(otherDisabilityCategoryMaxSize)),
      "needsSupportAtVenue" -> of(mayBeOptionalString("error.needsSupportAtVenue.required", 31, isFastStreamOrSdipFastStream)),
      "needsSupportAtVenueDescription" -> of(requiredFormatterWithMaxLengthCheck("needsSupportAtVenue", "needsSupportAtVenueDescription",
        Some(2048))),
      "needsSupportForPhoneInterview" -> of(mayBeOptionalString("error.needsSupportForPhoneInterview.required", 31, isEdipOrSdip)),
      "needsSupportForPhoneInterviewDescription" ->
        of(requiredFormatterWithMaxLengthCheck("needsSupportForPhoneInterview", "needsSupportForPhoneInterviewDescription", Some(2048)))
    )(AssistanceDetailsForm.Data.apply)(f => Some(Tuple.fromProductTyped(f)))
  )

  val hasDisability = "hasDisability"
  val disabilityImpact = "disabilityImpact"
  val disabilityCategories = "disabilityCategories"
  val otherDisabilityDescription = "otherDisabilityDescription"

  private def hasDisabilityFormatter(implicit messages: Messages) = new Formatter[String] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], String] = {
      val errorKey = "error.hasDisability.required"
      request.param(hasDisability) match {
        case Some(value) =>
          if (request.isHasDisabilityValid) {
            Right(value)
          } else {
            Left(List(FormError(key, Messages(errorKey))))
          }
        case _ =>
          Left(List(FormError(key, Messages(errorKey))))
      }
    }

    def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }

  private def disabilityImpactFormatter = new Formatter[Option[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      bindOptionalParam(request.isHasDisabilitySelected, request.isDisabilityImpactValid,
        "You must provide a valid disability impact")(key, request.disabilityImpactParam)
    }

    def unbind(key: String, value: Option[String]): Map[String, String] = optionalParamToMap(key, value)
  }

  def disabilityCategoriesFormatter = new Formatter[Option[List[String]]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[List[String]]] = {
      bindOptionalParam(request.isHasDisabilitySelected, request.isDisabilityCategoriesValid,
        "Choose a valid disability category")(key, request.disabilityCategoriesParam)
    }

    def unbind(key: String, value: Option[List[String]]): Map[String, String] = {
      value match {
        case Some(seq) => seq.zipWithIndex.foldLeft(Map.empty[String, String])(
          (res, pair) => res + (s"$key[${pair._2}]" -> pair._1))
        case None => Map.empty[String, String]
      }
    }
  }

  private def otherDisabilityDescriptionFormatter(maxSize: Int) = new Formatter[Option[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {

      val dependencyCheck = request.isOtherDisabilitySelected
      val isFilled = request.isOtherDisabilityDescriptionFilled
      val isCorrectSize = request.isOtherDisabilityDescriptionSizeValid(maxSize)

      (dependencyCheck, isFilled, isCorrectSize) match {
        case (true, true, false) => Left(List(FormError(key, s"The disability description must not exceed $maxSize characters")))
        case (true, true, true) => Right(Some(request.otherDisabilityDescriptionParam))
        case (true, false, _) => Right(None)
        case (false, _, _) => Right(None)
      }
    }

    def unbind(key: String, value: Option[String]): Map[String, String] = optionalParamToMap(key, value)
  }

  private def bindOptionalParam[T](dependencyCheck: Boolean, validityCheck: Boolean, errMsg: String)
                                  (key: String, value: => T):Either[Seq[FormError], Option[T]] = {
    (dependencyCheck, validityCheck) match {
      case (true, false) => Left(List(FormError(key, errMsg)))
      case (true, true) => Right(Some(value))
      case (false, _) => Right(None)
    }
  }

  private def optionalParamToMap[T](key: String, optValue: Option[T]) = {
    optValue match {
      case None => Map.empty[String, String]
      case Some(value) => Map(key -> value.toString)
    }
  }

  implicit class RequestValidation(request: Map[String, String]) {
    def param(name: String) = request.collectFirst { case (key, value) if key == name => value }

    def hasDisabilityParam = param(hasDisability).getOrElse("")

    val validHasDisabilityList = Seq("Yes", "No", "I don't know/prefer not to say")
    def isHasDisabilityValid = validHasDisabilityList.contains(hasDisabilityParam)

    def disabilityImpactParam = param(disabilityImpact).getOrElse("")

    val validDisabilityImpactList = Seq("Yes, a lot", "Yes, a little", "No")
    def isDisabilityImpactValid = validDisabilityImpactList.contains(disabilityImpactParam)

    def isHasDisabilitySelected = hasDisabilityParam == "Yes"

    def disabilityCategoriesParam = request.view.filterKeys(_.contains(disabilityCategories)).values.toList

    def isDisabilityCategoriesValid = {
      val preferNotToSayValid = if (disabilityCategoriesParam.contains(preferNotToSay)) {
        disabilityCategoriesParam.size == 1
      } else { true }

      // At least one disability category must be selected. The chosen categories must be ones from the list.
      // If "prefer not to say" is selected then it should be the only one
      disabilityCategoriesParam.nonEmpty && disabilityCategoriesParam.diff(disabilityCategoriesList).isEmpty && preferNotToSayValid
    }

    def isOtherDisabilitySelected = isHasDisabilitySelected && disabilityCategoriesParam.contains(other)

    def otherDisabilityDescriptionParam = param(otherDisabilityDescription).getOrElse("")

    def isOtherDisabilityDescriptionFilled = disabilityCategoriesParam.contains(other) && otherDisabilityDescriptionParam.nonEmpty

    // If Other is selected then the description must not be empty and not exceed the max size
    def isOtherDisabilityDescriptionSizeValid(max: Int) = disabilityCategoriesParam.contains(other) &&
      otherDisabilityDescriptionParam.nonEmpty && otherDisabilityDescriptionParam.length <= max
  }
}

object AssistanceDetailsForm {
  val preferNotToSay = "Prefer not to say"
  val other = "Other"

  // NOTE: If these values change remember to also change them in fset-faststream-admin-frontend report code
  //scalastyle:off line.size.limit
  val disabilityCategoriesList = List(
    "Learning difference such as dyslexia, dyspraxia or AD(H)D",
    "Social/communication conditions such as a speech and language impairment or an autistic spectrum condition",
    "Long-term illness or health condition such as cancer, HIV, diabetes, chronic heart disease, or epilepsy",
    "Mental health condition, challenge or disorder, such as depression, schizophrenia or anxiety",
    "Physical impairment (a condition that substantially limits one or more basic physical activities such as walking, climbing stairs, lifting or carrying)",
    "D/deaf or have a hearing impairment",
    "Blind or have a visual impairment uncorrected by glasses",
    "Development condition that you have had since childhood which affects motor, cognitive, social and emotional skills, and speech and language",
    "No known impairment, health condition or learning difference",
    "An impairment, health condition or learning difference not listed above",
    preferNotToSay,
    other
  ) //scalastyle:on

  case class Data(
    hasDisability: String,
    disabilityImpact: Option[String],
    disabilityCategories: Option[List[String]],
    otherDisabilityDescription: Option[String],
    needsSupportAtVenue: Option[String],
    needsSupportAtVenueDescription: Option[String],
    needsSupportForPhoneInterview: Option[String],
    needsSupportForPhoneInterviewDescription: Option[String]) {

    override def toString =
      s"hasDisability=$hasDisability," +
        s"disabilityImpact=$disabilityImpact," +
        s"disabilityCategories=$disabilityCategories," +
        s"otherDisabilityDescription=$otherDisabilityDescription," +
        s"needsSupportAtVenue=$needsSupportAtVenue," +
        s"needsSupportAtVenueDescription=$needsSupportAtVenueDescription," +
        s"needsSupportForPhoneInterview=$needsSupportForPhoneInterview," +
        s"needsSupportForPhoneInterviewDescription=$needsSupportForPhoneInterviewDescription"

    def exchange: AssistanceDetails = {
      AssistanceDetails(
        hasDisability,
        disabilityImpact,
        disabilityCategories,
        otherDisabilityDescription,
        AssistanceDetailsForm.Data.toOptBoolean(needsSupportAtVenue),
        needsSupportAtVenueDescription,
        AssistanceDetailsForm.Data.toOptBoolean(needsSupportForPhoneInterview),
        needsSupportForPhoneInterviewDescription
      )
    }

    def sanitizeData: AssistanceDetailsForm.Data = {
      def hasDisabilityCheck = hasDisability == "Yes"
      AssistanceDetailsForm.Data(
        hasDisability,
        if (hasDisabilityCheck) disabilityImpact else None,
        if (hasDisabilityCheck) disabilityCategories else None,
        if (hasDisabilityCheck) otherDisabilityDescription else None,
        needsSupportAtVenue,
        if (needsSupportAtVenue.contains("Yes")) needsSupportAtVenueDescription else None,
        needsSupportForPhoneInterview,
        if (needsSupportForPhoneInterview.contains("Yes")) needsSupportForPhoneInterviewDescription else None
      )
    }
  }

  object Data {
    def apply(ad: AssistanceDetails): AssistanceDetailsForm.Data = {
      Data(
        ad.hasDisability,
        ad.disabilityImpact,
        ad.disabilityCategories,
        ad.otherDisabilityDescription,
        toOptString(ad.needsSupportAtVenue),
        ad.needsSupportAtVenueDescription,
        toOptString(ad.needsSupportForPhoneInterview),
        ad.needsSupportForPhoneInterviewDescription
      )
    }

    def toOptBoolean(optString: Option[String]) = optString match {
      case Some("Yes") => Some(true)
      case Some("No") => Some(false)
      case _ => None
    }

    def toOptString(optBoolean: Option[Boolean]) = optBoolean match {
      case Some(true) => Some("Yes")
      case Some(false) => Some("No")
      case _ => None
    }
  }
}
