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
import models.ApplicationRoute
import models.view.CivilServantDepartments
import play.api.data.Forms.*
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.i18n.Messages

//scalastyle:off number.of.methods
object FastPassForm {

  val EmptyCivilServiceExperienceDetails: Option[Data] = Some(Data(applicable = ""))

  val CivilServant = "CivilServant"
  val CivilServantViaFastTrack = "CivilServantViaFastTrack"
  val DiversityInternship = "DiversityInternship"

  def sdipFsCivilServiceExperienceTypes(implicit messages: Messages): Seq[(String, String)] = Seq(
    CivilServant -> Messages("civilServiceExperienceType.CivilServant"),
    CivilServantViaFastTrack -> Messages("civilServiceExperienceType.CivilServantViaFastTrack"),
    DiversityInternship -> Messages("civilServiceExperienceType.EdipInternship")
  )

  val CivilServantKey = "CivilServant"
  val EDIPKey = "EDIP"
  val SDIPKey = "SDIP"
  val OtherInternshipKey = "OtherInternship"
  def civilServantAndInternshipTypes(implicit messages: Messages): Seq[(String, String)] = Seq(
    CivilServantKey -> Messages("civilServantAndInternshipType.civilServant"),
    EDIPKey -> Messages("civilServantAndInternshipType.EDIP"),
    SDIPKey -> Messages("civilServantAndInternshipType.SDIP"),
    OtherInternshipKey -> Messages("civilServantAndInternshipType.another")
  )

  private val civilServantDepartment = "civilServantDepartment"
  private def civilServantDepartmentMsg(implicit messages: Messages) = Messages("error.civilServantDepartment.required")

  private val liveDisciplinaryWarning = "liveDisciplinaryWarning"
  private def liveDisciplinaryWarningRequiredMsg(implicit messages: Messages) = Messages("error.liveDisciplinaryWarning.required")
  private def liveDisciplinaryWarningCannotContinueMsg(implicit messages: Messages) = Messages("error.liveDisciplinaryWarning.cannot.continue")

  private val inReviewPeriodFollowingAWarning = "inReviewPeriodFollowingAWarning"
  private def inReviewPeriodFollowingAWarningRequiredMsg(implicit messages: Messages) =
    Messages("error.inReviewPeriodFollowingAWarning.required")
  private def inReviewPeriodFollowingAWarningCannotContinueMsg(implicit messages: Messages) =
    Messages("error.inReviewPeriodFollowingAWarning.cannot.continue")

  private val inImprovementPeriodFollowingAWarning = "inImprovementPeriodFollowingAWarning"
  private def inImprovementPeriodFollowingAWarningRequiredMsg(implicit messages: Messages) =
    Messages("error.inImprovementPeriodFollowingAWarning.required")
  private def inImprovementPeriodFollowingAWarningCannotContinueMsg(implicit messages: Messages) =
    Messages("error.inImprovementPeriodFollowingAWarning.cannot.continue")

  private val edipInternshipYear = "edipYear"
  private def edipInternshipYearMsg(implicit messages: Messages) = Messages("error.edipInternshipYear.required")

  private val sdipInternshipYear = "sdipYear"
  private def sdipInternshipYearMsg(implicit messages: Messages) = Messages("error.sdipInternshipYear.required")

  private val otherInternshipName = "otherInternshipName"
  private def otherInternshipNameMsg(implicit messages: Messages) = Messages("error.otherInternshipName.required")
  protected[forms] val otherInternshipNameMaxSize = 60
  private def otherInternshipNameSizeMsg(implicit messages: Messages) = Messages("error.otherInternshipName.size", otherInternshipNameMaxSize)

  val otherInternshipYear = "otherInternshipYear"
  private def otherInternshipYearMsg(implicit messages: Messages) = Messages("error.otherInternshipYear.required")

  private def civilServantAndInternshipTypeRequiredMsg(implicit messages: Messages) = Messages("error.civilServantAndInternshipTypes.required")

  private def fastPassReceivedRequiredMsg(implicit messages: Messages) = Messages("error.fastPassReceived.required")
  private def certificateNumberRequiredMsg(implicit messages: Messages) = Messages("error.certificateNumber.required")

  val formQualifier = "civilServiceExperienceDetails"
  val applicable = "applicable"
  val civilServantAndInternshipTypesKey = "civilServantAndInternshipTypes"
  val fastPassReceived = "fastPassReceived"
  val certificateNumber = "certificateNumber"

  case class Data(applicable: String,
                  civilServantAndInternshipTypes: Option[Seq[String]] = None,
                  civilServantDepartment: Option[String] = None,
                  liveDisciplinaryWarning: Option[Boolean] = None,
                  inReviewPeriodFollowingAWarning: Option[Boolean] = None,
                  inImprovementPeriodFollowingAWarning: Option[Boolean] = None,
                  edipYear: Option[String] = None,
                  sdipYear: Option[String] = None,
                  otherInternshipName: Option[String] = None,
                  otherInternshipYear: Option[String] = None,
                  fastPassReceived: Option[Boolean] = None,
                  certificateNumber: Option[String] = None) {

    override def toString: String =
      "(" +
        s"applicable=$applicable," +
        s"civilServantAndInternshipTypes=$civilServantAndInternshipTypes," +
        s"civilServantDepartment=$civilServantDepartment," +
        s"liveDisciplinaryWarning=$liveDisciplinaryWarning," +
        s"inReviewPeriodFollowingAWarning=$inReviewPeriodFollowingAWarning," +
        s"inImprovementPeriodFollowingAWarning=$inImprovementPeriodFollowingAWarning," +
        s"edipYear=$edipYear," +
        s"sdipYear=$sdipYear," +
        s"otherInternshipName=$otherInternshipName," +
        s"otherInternshipYear=$otherInternshipYear," +
        s"fastPassReceived=$fastPassReceived," +
        s"certificateNumber=$certificateNumber" +
        ")"
  }

  def form(implicit messages: Messages): Form[Data] = {
    Form(mapping(
      s"$formQualifier.applicable" -> nonemptyBooleanText("error.applicable.required"),
      s"$formQualifier.civilServantAndInternshipTypes" -> of(civilServantAndInternshipTypesFormatter),
      s"$formQualifier.civilServantDepartment" -> of(civilServantDepartmentFormatter),
      s"$formQualifier.liveDisciplinaryWarning" -> of(liveDisciplinaryWarningFormatter),
      s"$formQualifier.inReviewPeriodFollowingAWarning" -> of(inReviewPeriodFollowingAWarningFormatter),
      s"$formQualifier.inImprovementPeriodFollowingAWarning" -> of(inImprovementPeriodFollowingAWarningFormatter),
      s"$formQualifier.edipYear" -> of(edipInternshipYearFormatter),
      s"$formQualifier.sdipYear" -> of(sdipInternshipYearFormatter),
      s"$formQualifier.otherInternshipName" -> of(otherInternshipNameFormatter(otherInternshipNameMaxSize)),
      s"$formQualifier.otherInternshipYear" -> of(otherInternshipYearFormatter),
      s"$formQualifier.fastPassReceived" -> of(fastPassReceivedFormatter),
      s"$formQualifier.certificateNumber" -> of(fastPassCertificateFormatter)
    )(Data.apply)(f => Some(Tuple.fromProductTyped(f))))
  }

  // Only applicable for fs candidates - all other application routes have validation on the PersonalDetailsForm
  private def civilServantAndInternshipTypesFormatter(implicit messages: Messages) = new Formatter[Option[Seq[String]]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[Seq[String]]] = {
      bindOptionalParam(request.isCivilServantOrIntern && request.isFaststream, request.isValidCivilServantAndInternshipTypeSelected,
        civilServantAndInternshipTypeRequiredMsg)(key, request.civilServantAndInternshipTypesParam)
    }

    def unbind(key: String, value: Option[Seq[String]]): Map[String, String] = {
      value match {
        case Some(seq) => seq.zipWithIndex.foldLeft(Map.empty[String, String])(
          (res, pair) => res + (s"$key[${pair._2}]" -> pair._1))
        case None => Map.empty[String, String]
      }
    }
  }

  private def sdipInternshipYearFormatter(implicit messages: Messages) = new Formatter[Option[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      bindOptionalParam(request.isSdipCandidate, request.isSdipInternshipYearValid,
        sdipInternshipYearMsg)(key, request.sdipInternshipYearParam)
    }

    def unbind(key: String, value: Option[String]): Map[String, String] = optionalParamToMap(key, value)
  }

  private def edipInternshipYearFormatter(implicit messages: Messages) = new Formatter[Option[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      bindOptionalParam(request.isEdipCandidate, request.isEdipInternshipYearValid,
        edipInternshipYearMsg)(key, request.edipInternshipYearParam)
    }

    def unbind(key: String, value: Option[String]): Map[String, String] = optionalParamToMap(key, value)
  }

  def otherInternshipNameFormatter(maxSize: Int)(implicit messages: Messages): Formatter[Option[String]] = new Formatter[Option[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {

      val dependencyCheck = request.isOtherInternshipCandidate
      val isFilled = request.isOtherInternshipNameFilled
      val isCorrectSize = request.isOtherInternshipNameSizeValid(maxSize)

      (dependencyCheck, isFilled, isCorrectSize) match {
        case (true, false, _) => Left(List(FormError(key, otherInternshipNameMsg)))
        case (true, true, false) => Left(List(FormError(key, otherInternshipNameSizeMsg)))
        case (true, true, true) => Right(Some(request.otherInternshipNameParam))
        case (false, _, _) => Right(None)
      }
    }

    def unbind(key: String, value: Option[String]): Map[String, String] = optionalParamToMap(key, value)
  }

  def otherInternshipYearFormatter(implicit messages: Messages): Formatter[Option[String]] = new Formatter[Option[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      bindOptionalParam(request.isOtherInternshipCandidate, request.isOtherInternshipYearValid,
        otherInternshipYearMsg)(key, request.otherInternshipYearParam)
    }

    def unbind(key: String, value: Option[String]): Map[String, String] = optionalParamToMap(key, value)
  }

  private def fastPassReceivedFormatter(implicit messages: Messages) = new Formatter[Option[Boolean]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[Boolean]] = {
      bindOptionalParam(request.isCivilServantOrIntern, request.isFastPassReceivedValid,
        fastPassReceivedRequiredMsg)(key, request.fastPassReceivedParam.toBoolean)
    }

    def unbind(key: String, value: Option[Boolean]): Map[String, String] = optionalParamToMap(key, value)
  }

  private def fastPassCertificateFormatter(implicit messages: Messages) = new Formatter[Option[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      bindOptionalParam(request.isFastPassReceived, request.isCertificateNumberValid,
        certificateNumberRequiredMsg)(key, request.certificateNumberParam)
    }

    def unbind(key: String, value: Option[String]): Map[String, String] = optionalParamToMap(key, value)
  }

  private def civilServantDepartmentFormatter(implicit messages: Messages) = new Formatter[Option[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      bindOptionalParam(request.isCivilServantSelected, request.isCivilServantDepartmentValid,
        civilServantDepartmentMsg)(key, request.civilServantDepartmentParam)
    }

    def unbind(key: String, value: Option[String]): Map[String, String] = optionalParamToMap(key, value)
  }

  private def liveDisciplinaryWarningFormatter(implicit messages: Messages) = new Formatter[Option[Boolean]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[Boolean]] = {
      bindOptionalParam(
        request.isCivilServantSelected,
        request.isLiveDisciplinaryWarningValid, liveDisciplinaryWarningRequiredMsg,
        request.hasLiveDisciplinaryWarning, liveDisciplinaryWarningCannotContinueMsg
      )(key, request.liveDisciplinaryWarningParam.toBoolean)
    }

    def unbind(key: String, value: Option[Boolean]): Map[String, String] = optionalParamToMap(key, value)
  }

  private def inReviewPeriodFollowingAWarningFormatter(implicit messages: Messages) = new Formatter[Option[Boolean]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[Boolean]] = {
      bindOptionalParam(
        request.isCivilServantSelected,
        request.isInReviewPeriodFollowingAWarningValid, inReviewPeriodFollowingAWarningRequiredMsg,
        request.hasInReviewPeriodFollowingAWarningWarning, inReviewPeriodFollowingAWarningCannotContinueMsg
      )(key, request.inReviewPeriodFollowingAWarningParam.toBoolean)
    }

    def unbind(key: String, value: Option[Boolean]): Map[String, String] = optionalParamToMap(key, value)
  }

  private def inImprovementPeriodFollowingAWarningFormatter(implicit messages: Messages) = new Formatter[Option[Boolean]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], Option[Boolean]] = {
      bindOptionalParam(
        request.isCivilServantSelected,
        request.isInImprovementPeriodFollowingAWarningValid, inImprovementPeriodFollowingAWarningRequiredMsg,
        request.hasInImprovementPeriodFollowingAWarningWarning, inImprovementPeriodFollowingAWarningCannotContinueMsg
      )(key, request.inImprovementPeriodFollowingAWarningParam.toBoolean)
    }

    def unbind(key: String, value: Option[Boolean]): Map[String, String] = optionalParamToMap(key, value)
  }

  private def bindOptionalParam[T](dependencyCheck: Boolean, validityCheck: Boolean, errMsg: String)
                                  (key: String, value: => T): Either[Seq[FormError], Option[T]] =
    (dependencyCheck, validityCheck) match {
      case (true, false) => Left(List(FormError(key, errMsg)))
      case (true, true) => Right(Some(value))
      case (false, _) => Right(None)
    }

  // This version supports a validity check that checks the value submitted is correct, followed by 2nd a check
  // to see if the candidate has a warning and so cannot continue their application
  private def bindOptionalParam[T](dependencyCheck: Boolean,
                                   validityCheck: Boolean, validityErrMsg: String,
                                   hasWarning: Boolean, warningErrMsg: String)
                                  (key: String, value: => T): Either[Seq[FormError], Option[T]] = {
    (dependencyCheck, validityCheck) match {
      case (true, false) => Left(List(FormError(key, validityErrMsg)))
      case (true, true) =>
        if (hasWarning) {
          Left(List(FormError(key, warningErrMsg)))
        } else {
          Right(Some(value))
        }
      case (false, _) => Right(None)
    }
  }

  private def optionalParamToMap[T](key: String, optValue: Option[T]) = {
    optValue match {
      case None => Map.empty[String, String]
      case Some(value) => Map(key -> value.toString)
    }
  }

  //scalastyle:off number.of.methods
  implicit class RequestValidation(request: Map[String, String]) {

    def param(name: String): Option[String] = request.collectFirst { case (key, value) if key.endsWith(name) => value }

    private def fastPassApplicableParam = param(applicable).getOrElse("")

    def isCivilServantOrIntern: Boolean = fastPassApplicableParam == "true"

    def isFaststream: Boolean = request.get("applicationRoute").contains(ApplicationRoute.Faststream.toString)

    def civilServantAndInternshipTypesParam(implicit messages: Messages): Seq[String] = {
      request.view.filterKeys(_.contains(civilServantAndInternshipTypesKey)).values.toSeq
    }

    def isValidCivilServantAndInternshipTypeSelected(implicit messages: Messages): Boolean = {
      val filled = civilServantAndInternshipTypesParam.nonEmpty
      val isValid = civilServantAndInternshipTypesParam.diff(civilServantAndInternshipTypes.toMap.keys.toSeq).isEmpty
      filled && isValid
    }

    // Civil servant
    def isCivilServantSelected(implicit messages: Messages): Boolean = isCivilServantOrIntern &&
      civilServantAndInternshipTypesParam.contains(CivilServantKey)

    def civilServantDepartmentParam: String = param(civilServantDepartment).getOrElse("")

    def isCivilServantDepartmentValid: Boolean = CivilServantDepartments.departments.contains(civilServantDepartmentParam)

    def liveDisciplinaryWarningParam: String = param(liveDisciplinaryWarning).getOrElse("")

    def isLiveDisciplinaryWarningValid: Boolean = liveDisciplinaryWarningParam == "true" || liveDisciplinaryWarningParam == "false"

    def hasLiveDisciplinaryWarning: Boolean = isCivilServantOrIntern && liveDisciplinaryWarningParam == "true"

    def inReviewPeriodFollowingAWarningParam: String = param(inReviewPeriodFollowingAWarning).getOrElse("")

    def isInReviewPeriodFollowingAWarningValid: Boolean =
      inReviewPeriodFollowingAWarningParam == "true" || inReviewPeriodFollowingAWarningParam == "false"

    def hasInReviewPeriodFollowingAWarningWarning: Boolean = isCivilServantOrIntern && inReviewPeriodFollowingAWarningParam == "true"

    def inImprovementPeriodFollowingAWarningParam: String = param(inImprovementPeriodFollowingAWarning).getOrElse("")

    def isInImprovementPeriodFollowingAWarningValid: Boolean =
      inImprovementPeriodFollowingAWarningParam == "true" || inImprovementPeriodFollowingAWarningParam == "false"

    def hasInImprovementPeriodFollowingAWarningWarning: Boolean = isCivilServantOrIntern && inImprovementPeriodFollowingAWarningParam == "true"

    // Sdip
    def sdipInternshipYearParam: String = param(sdipInternshipYear).getOrElse("")

    def isSdipCandidate(implicit messages: Messages): Boolean = isCivilServantOrIntern && isSdipInternshipSelected

    private def isSdipInternshipSelected(implicit messages: Messages) = civilServantAndInternshipTypesParam.contains(SDIPKey)

    def isSdipInternshipYearValid(implicit messages: Messages): Boolean = isSdipInternshipSelected && sdipInternshipYearParam.matches("[0-9]{4}")

    // Edip
    def edipInternshipYearParam: String = param(edipInternshipYear).getOrElse("")

    def isEdipCandidate(implicit messages: Messages): Boolean = isCivilServantOrIntern && civilServantAndInternshipTypesParam.contains(EDIPKey)

    private def isEdipInternshipSelected(implicit messages: Messages) = civilServantAndInternshipTypesParam.contains(EDIPKey)

    def isEdipInternshipYearValid(implicit messages: Messages): Boolean = isEdipInternshipSelected && edipInternshipYearParam.matches("[0-9]{4}")

    // Other internship
    private def isOtherInternshipSelected(implicit messages: Messages) = civilServantAndInternshipTypesParam.contains(OtherInternshipKey)

    def isOtherInternshipCandidate(implicit messages: Messages): Boolean = isCivilServantOrIntern && isOtherInternshipSelected

    // Other internship name
    def otherInternshipNameParam: String = param(otherInternshipName).getOrElse("")

    def isOtherInternshipNameFilled(implicit messages: Messages): Boolean = isOtherInternshipSelected && otherInternshipNameParam.nonEmpty

    def isOtherInternshipNameSizeValid(max: Int)(implicit messages: Messages): Boolean = isOtherInternshipSelected &&
      isOtherInternshipNameFilled && otherInternshipNameParam.length <= max

    // Other internship year
    def otherInternshipYearParam: String = param(otherInternshipYear).getOrElse("")

    def isOtherInternshipYearValid(implicit messages: Messages): Boolean =
      isOtherInternshipSelected && otherInternshipYearParam.matches("[0-9]{4}")

    // Fast pass received
    def fastPassReceivedParam: String = param(fastPassReceived).getOrElse("")

    def isFastPassReceived: Boolean = isCivilServantOrIntern && fastPassReceivedParam == "true"

    def isFastPassReceivedValid: Boolean = fastPassReceivedParam == "true" || fastPassReceivedParam == "false"

    // Fast pass certificate
    def certificateNumberParam: String = param(certificateNumber).getOrElse("")

    def isCertificateNumberValid: Boolean = certificateNumberParam.matches("[0-9]{7}")

    // Removes child data that is dependent on a parent if that parent has not been selected
    //scalastyle:off cyclomatic.complexity
    def cleanupFastPassFields(implicit messages: Messages): Map[String, String] = request.view.filterKeys {
      case key if key.contains("civilServantAndInternshipTypes") || key.contains("fastPassReceived") => isCivilServantOrIntern
      case key if key.endsWith("civilServantDepartment") => isCivilServantSelected
      case key if key.endsWith("liveDisciplinaryWarning") => isCivilServantSelected
      case key if key.endsWith("inReviewPeriodFollowingAWarning") => isCivilServantSelected
      case key if key.endsWith("inImprovementPeriodFollowingAWarning") => isCivilServantSelected
      case key if key.endsWith("sdipYear") => isSdipCandidate
      case key if key.endsWith("otherInternshipName") || key.endsWith("otherInternshipYear") => isOtherInternshipCandidate
      case key if key.endsWith("edipYear") => isEdipCandidate
      case key if key.endsWith("certificateNumber") => isFastPassReceived
      case _ => true
    }.toMap //scalastyle:on
  }
}
