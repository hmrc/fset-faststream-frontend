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

package services

import factories.DateTimeFactory

import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

object TimeFormattingService extends TimeFormattingService {
  val dateTimeFactory: DateTimeFactory = DateTimeFactory
}

trait TimeFormattingService {

  val dateTimeFactory: DateTimeFactory

  case class DateTimeDifference(years: Long, months: Long, days: Long, hours: Long, minutes: Long)

  def durationFromNow(date: OffsetDateTime): String = {
    val now = dateTimeFactory.nowDateTime
    val dateTimeDifference = getDateTimeDifference(now, date)

    val yearPart = getPart(dateTimeDifference.years, "year")
    val monthPart = getPart(dateTimeDifference.months, "month")
    val daysPart = getPart(dateTimeDifference.days, "day")
    val hoursPart = getPart(dateTimeDifference.hours, "hour")
    val mainPart = yearPart + monthPart + daysPart + hoursPart
    val sanitisedMainPart = if (mainPart.endsWith(", ")) mainPart.substring(0, mainPart.length - 2) else mainPart
    val minutesPart =
      if ((!sanitisedMainPart.isEmpty && dateTimeDifference.minutes > 0) || sanitisedMainPart.isEmpty) {
        getPartWithZero(dateTimeDifference.minutes, "minute")
      } else {
        ""
      }

    val andPart = if (sanitisedMainPart.isEmpty || minutesPart.isEmpty) "" else " and "
    sanitisedMainPart + andPart + minutesPart
  }

  def durationFromNowWithMoreZeros(date: OffsetDateTime): String = {
    val now = dateTimeFactory.nowDateTime
    val dateTimeDifference = getDateTimeDifference(now, date)

    val yearPart = getPart(dateTimeDifference.years, "year")
    val monthPart = getPart(dateTimeDifference.months, "month")
    val daysPart = getPartWithZero(dateTimeDifference.days, "day")
    val hoursPart = getPartWithZero(dateTimeDifference.hours, "hour")
    val mainPart = yearPart + monthPart + daysPart + hoursPart
    val sanitisedMainPart = mainPart.substring(0, mainPart.length - 2)
    val minutesPart = getPartWithZero(dateTimeDifference.minutes, "minute")
    sanitisedMainPart + " and " + minutesPart
  }

  private def getDateTimeDifference(startDate: OffsetDateTime, endDate: OffsetDateTime): DateTimeDifference = {
    val years = startDate.until(endDate, ChronoUnit.YEARS)
    var endDate2 = endDate.minusYears(years)
    val months = startDate.until(endDate2, ChronoUnit.MONTHS)
    endDate2 = endDate2.minusMonths(months)
    val days = startDate.until(endDate2, ChronoUnit.DAYS)
    endDate2 = endDate2.minusDays(days)
    val hours = startDate.until(endDate2, ChronoUnit.HOURS)
    endDate2 = endDate2.minusHours(hours)
    val minutes = startDate.until(endDate2, ChronoUnit.MINUTES)
    DateTimeDifference(years, months, days, hours, minutes)
  }

  private def getSingularOrPluralWording(value: Long): String = {
    if (value == 1) "" else "s"
  }

  private def getPart(value: Long, name: String): String = {
    if (value == 0) {
      ""
    } else {
      s"$value $name${getSingularOrPluralWording(value)}, "
    }
  }

  private def getPartWithZero(value: Long, name: String): String = {
    val base = s"$value $name${getSingularOrPluralWording(value)}"
    if (name.equalsIgnoreCase("minute")) {
      base
    } else {
      s"${base}, "
    }
  }
}
