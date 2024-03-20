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
import helpers.Timezones
import org.mockito.Mockito._
import testkit.UnitSpec

import java.time.{LocalDateTime, OffsetDateTime}

class TimeFormattingServiceSpec extends UnitSpec {

  "Time formatting service" should {
    "correctly display years, months, days, hours and minutes remaining until expiry" in new TestFixture {
      val expiryDate = now.plusYears(3).plusMonths(3).plusDays(3).plusHours(3).plusMinutes(11)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "3 years, 3 months, 3 days, 3 hours and 11 minutes"
    }

    "correctly display months, days, hours and minutes remaining until expiry" in new TestFixture {
      val expiryDate = now.plusMonths(3).plusDays(3).plusHours(3).plusMinutes(11)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "3 months, 3 days, 3 hours and 11 minutes"
    }

    "correctly display days, hours and minutes remaining until expiry" in new TestFixture {
      val expiryDate = now.plusDays(3).plusHours(3).plusMinutes(11)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "3 days, 3 hours and 11 minutes"
    }

    "correctly display hours and minutes remaining until expiry" in new TestFixture {
      val expiryDate = now.plusHours(3).plusMinutes(11)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "3 hours and 11 minutes"
    }

    "correctly display minutes remaining until expiry" in new TestFixture {
      val expiryDate = now.plusMinutes(11)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "11 minutes"
    }

    "correctly display zero minutes remaining until expiry when we have less than a minute remaining" in new TestFixture {
      val expiryDate = now.plusSeconds(10)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "0 minutes"
    }

    "correctly display zero minutes remaining until expiry when the time now is the expiry time" in new TestFixture {
      val expiryDate = now
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "0 minutes"
    }

    "correctly display negative minutes remaining once we are past expiry" in new TestFixture {
      val expiryDate = now.minusMinutes(1)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "-1 minutes"
    }

    "correctly display years remaining until expiry" in new TestFixture {
      val expiryDate = now.plusYears(3)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "3 years"
    }

    "correctly display months remaining until expiry" in new TestFixture {
      val expiryDate = now.plusMonths(3)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "3 months"
    }

    "correctly display days remaining until expiry" in new TestFixture {
      val expiryDate = now.plusDays(3)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "3 days"
    }

    "correctly display hours remaining until expiry" in new TestFixture {
      val expiryDate = now.plusHours(3)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "3 hours"
    }

    "correctly display year, month, day, hour and minute remaining until expiry" in new TestFixture {
      val expiryDate = now.plusYears(1).plusMonths(1).plusDays(1).plusHours(1).plusMinutes(1)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "1 year, 1 month, 1 day, 1 hour and 1 minute"
    }

    "correctly display month, hours and minutes remaining until expiry" in new TestFixture {
      val expiryDate = now.plusMonths(1).plusHours(4).plusMinutes(10)
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "1 month, 4 hours and 10 minutes"
    }

    "correctly display days, hours and minutes remaining until expiry, dates in some offset/ zone (no British summer time)" in new TestFixture {
      override val now = LocalDateTime.parse("2023-03-14T16:35:13.185").atZone(Timezones.londonDateTimeZone).toOffsetDateTime
      when(dateTimeFactoryMock.nowDateTime).thenReturn(now)

      val expiryDate = LocalDateTime.parse("2023-03-21T14:40:13.185").atZone(Timezones.londonDateTimeZone).toOffsetDateTime
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "6 days, 22 hours and 5 minutes"
    }

    "correctly display days, hours and minutes remaining until expiry, dates in some offset/ zone (British summer time)" in new TestFixture {
      override val now = LocalDateTime.parse("2023-03-21T16:35:13.185").atZone(Timezones.londonDateTimeZone).toOffsetDateTime
      when(dateTimeFactoryMock.nowDateTime).thenReturn(now)

      val expiryDate = LocalDateTime.parse("2023-03-28T14:40:13.185").atZone(Timezones.londonDateTimeZone).toOffsetDateTime
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "6 days, 21 hours and 5 minutes"
    }

    "correctly display hours remaining until expiry, dates in some offset/ zone (British summer time)" in new TestFixture {
      override val now = LocalDateTime.parse("2023-03-25T16:35:13.185").atZone(Timezones.londonDateTimeZone).toOffsetDateTime
      when(dateTimeFactoryMock.nowDateTime).thenReturn(now)

      val expiryDate = LocalDateTime.parse("2023-03-26T16:35:13.185").atZone(Timezones.londonDateTimeZone).toOffsetDateTime
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "23 hours"
    }

    "correctly display negative hours remaining until expiry" in new TestFixture {
      override val now = OffsetDateTime.parse("2023-03-25T16:35:13.185+00:00")
      when(dateTimeFactoryMock.nowDateTime).thenReturn(now)

      val expiryDate = OffsetDateTime.parse("2023-03-25T16:35:13.185+03:00")
      val durationRemaining = service.durationFromNow(expiryDate)
      durationRemaining mustBe "-3 hours"
    }
  }

  trait TestFixture {
    val dateTimeFactoryMock = mock[DateTimeFactory]
    val now = OffsetDateTime.parse("2018-10-01T12:10:00Z")
    when(dateTimeFactoryMock.nowDateTime).thenReturn(now)

    val service = new TimeFormattingService {
      val dateTimeFactory = dateTimeFactoryMock
    }
  }
}
