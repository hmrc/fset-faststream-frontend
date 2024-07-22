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

package connectors.exchange.referencedata

import play.api.libs.json._

case class LocationId(value: String)

object LocationId {
  // Custom json formatter to serialise to and from a json string
  val locationIdWritesFormat: Writes[LocationId] = Writes[LocationId](location => JsString(location.value))
  val locationIdReadsFormat: Reads[LocationId] = Reads[LocationId](location => JsSuccess(LocationId(location.as[String])))

  implicit val locationIdFormat: Format[LocationId] = Format(locationIdReadsFormat, locationIdWritesFormat)
}

case class SdipLocation(
  id: LocationId,
  name: String
)

object SdipLocation {
  implicit val locationFormat: OFormat[SdipLocation] = Json.format[SdipLocation]

  def apply(id: String, name: String): SdipLocation =
    SdipLocation(LocationId(id), name)
}
