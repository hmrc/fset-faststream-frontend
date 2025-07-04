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

package connectors.exchange

import forms.FastPassForm
import forms.FastPassForm.Data
import play.api.libs.json.{Json, OFormat}

import language.implicitConversions

case class CivilServiceExperienceDetails(applicable: Boolean,
                                        civilServantAndInternshipTypes: Option[Seq[String]] = None,
                                        civilServantDepartment: Option[String] = None,
                                        edipYear: Option[String] = None,
                                        sdipYear: Option[String] = None,
                                        otherInternshipName: Option[String] = None,
                                        otherInternshipYear: Option[String] = None,
                                        fastPassReceived: Option[Boolean] = None,
                                        fastPassAccepted: Option[Boolean] = None,
                                        certificateNumber: Option[String] = None) {
  override def toString: String = {
    s"applicable=$applicable," +
      s"civilServantAndInternshipTypes=$civilServantAndInternshipTypes," +
      s"civilServantDepartment=$civilServantDepartment," +
      s"edipYear=$edipYear," +
      s"sdipYear=$sdipYear," +
      s"otherInternshipName=$otherInternshipName," +
      s"otherInternshipYear=$otherInternshipYear," +
      s"fastPassReceived=$fastPassReceived," +
      s"fastPassAccepted=$fastPassAccepted," +
      s"certificateNumber=$certificateNumber"
  }

  def isCivilServant =
    civilServantAndInternshipTypes.exists { types => types.contains(FastPassForm.CivilServantKey) }
}

object CivilServiceExperienceDetails {

  implicit val civilServiceExperienceDetailsFormat: OFormat[CivilServiceExperienceDetails] = Json.format[CivilServiceExperienceDetails]

  implicit def toData(optExchange: Option[CivilServiceExperienceDetails]): Option[Data] = optExchange.map(exchange => Data(
    applicable = exchange.applicable.toString,
    civilServantAndInternshipTypes = exchange.civilServantAndInternshipTypes,
    civilServantDepartment = exchange.civilServantDepartment,
    edipYear = exchange.edipYear,
    sdipYear = exchange.sdipYear,
    otherInternshipName = exchange.otherInternshipName,
    otherInternshipYear = exchange.otherInternshipYear,
    fastPassReceived = exchange.fastPassReceived,
    certificateNumber = exchange.certificateNumber
  ))

  implicit def toExchange(optData: Option[Data]): Option[CivilServiceExperienceDetails] = {
    optData.map( data =>
      CivilServiceExperienceDetails(
        applicable = data.applicable.toBoolean,
        civilServantAndInternshipTypes = data.civilServantAndInternshipTypes,
        civilServantDepartment = data.civilServantDepartment,
        edipYear = data.edipYear,
        sdipYear = data.sdipYear,
        otherInternshipName = data.otherInternshipName,
        otherInternshipYear = data.otherInternshipYear,
        fastPassReceived = data.fastPassReceived,
        fastPassAccepted = None,
        certificateNumber = data.certificateNumber
      )
    )
  }
}
