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

import connectors.exchange.SelectedLocations
import connectors.exchange.referencedata.SdipLocation
import forms.SelectedLocationsForm.{ interestsList, LocationPreferences }
import models.page.SelectedLocationsPage
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import play.api.i18n.Messages

import scala.language.implicitConversions

class SelectedLocationsForm(locations: Seq[SdipLocation]) {

  private val page = SelectedLocationsPage(locations)

  private val maxLocations = 4

  def form(implicit messages: Messages) = {
    Form(
      mapping(
        "locations" -> of(locationFormatter("locations")),
        "interests" -> of(interestsFormatter)
      )(LocationPreferences.apply)(f => Some(Tuple.fromProductTyped(f)))
    )
  }

  //scalastyle:off cyclomatic.complexity
  def locationFormatter(formKey: String)(implicit messages: Messages) = new Formatter[List[String]] {
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], List[String]] = {
      page.getLocationsByPriority(data) match {
        case selectedLocations if selectedLocations.isEmpty =>
          Left(List(FormError(formKey, Messages("locations.required"))))
        case selectedLocations if page.getInvalidLocations(selectedLocations).nonEmpty =>
          Left(List(FormError(formKey, Messages("locations.required"))))
        case selectedLocations if selectedLocations.size > maxLocations =>
          Left(List(FormError(formKey, Messages("locations.tooMany"))))
        case selectedLocations =>
          Right(selectedLocations)
      }
    }

    def unbind(key: String, value: List[String]): Map[String, String] = {
      value.map(key => key -> Messages("location." + key + ".description")).toMap
    }
  } //scalastyle:on

  def interestsFormatter = new Formatter[List[String]] {
    def bind(key: String, request: Map[String, String]): Either[Seq[FormError], List[String]] = {
      bindParam(request.areInterestsValid,"You must choose an interest")(key, request.interestsParam)
    }
    def unbind(key: String, value: List[String]): Map[String, String] = {
      value.zipWithIndex.foldLeft(Map.empty[String, String])(
        (res, pair) => res + (s"$key[${pair._2}]" -> pair._1))
    }
  }

  private def bindParam[T](validityCheck: Boolean, errMsg: String)
                          (key: String, value: => T): Either[Seq[FormError], T] = {
    validityCheck match {
      case false => Left(List(FormError(key, errMsg)))
      case true => Right(value)
    }
  }

  implicit class RequestValidation(request: Map[String, String]) {
    def interestsParam = request.view.filterKeys(_.contains("interests")).values.toList

    def areInterestsValid = {
      // At least one interest must be selected and the chosen interests must be ones from the list.
      interestsParam.nonEmpty && interestsParam.diff(interestsList).isEmpty
    }
  }
}

object SelectedLocationsForm {

  val interestsList = List(
    "Commercial",
    "Cyber",
    "Digital and Data",
    "Diplomacy and International Relations",
    "Economics",
    "Finance",
    "Governance",
    "Operational Delivery",
    "People",
    "Policy Development",
    "Project Management",
    "Science and Engineering",
    "Statistics and Research"
  )

  case class LocationPreferences(locations: List[String], interests: List[String])

  /**
   * Used to convert from the selected locations (List[String]) read from the db and convert to the LocationPreferences
   * (List[String]) that are used in the form when presenting
   */
  implicit def toLocationPreferences(selectedLocations: SelectedLocations): LocationPreferences =
    LocationPreferences(selectedLocations.locations, selectedLocations.interests)

  /**
   * Used to convert from submitted LocationPreferences to SelectedLocations in preparation for
   * storing the selected locations in the database
   */
  implicit def toSelectedSchemes(locationPreferences: LocationPreferences): SelectedLocations =
    SelectedLocations(locationPreferences.locations, locationPreferences.interests)
}
