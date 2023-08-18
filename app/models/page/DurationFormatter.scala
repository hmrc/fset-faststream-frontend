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

package models.page

import factories.DateTimeFactory
import helpers.Timezones
import services.TimeFormattingService

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

trait DurationFormatter extends TimeFormattingService {

  def expirationDate: OffsetDateTime

  val dateTimeFactory: DateTimeFactory = DateTimeFactory

  private[page] def now = OffsetDateTime.now

  def getDuration: String = {
    durationFromNowWithMoreZeros(expirationDate)
  }

  private val expireTimeFormatter = DateTimeFormatter.ofPattern("h:mma")

  private val expireDateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

  private val expirationDateLondon = expirationDate.toZonedDateTime.withZoneSameInstant(Timezones.londonDateTimeZone).toOffsetDateTime

  private def timeFormat(dateTime: OffsetDateTime) = expireTimeFormatter.format(dateTime).toLowerCase

  private def dateFormat(dateTime: OffsetDateTime) = expireDateFormatter.format(dateTime)

  def getExpireTime: String = timeFormat(expirationDate)

  def getExpireTimeLondon: String = timeFormat(expirationDateLondon)

  def getExpireDate: String = dateFormat(expirationDate)

  def getExpireDateLondon: String = dateFormat(expirationDateLondon)
}
