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

import org.scalatestplus.play.PlaySpec

import java.time.{OffsetDateTime, ZoneOffset}

class DurationFormatterSpec extends PlaySpec {

  "Duration formatter" should {
    "return years, months, days and hours" in new TestFixture {
      val futureDate = now.plusYears(10).plusMonths(1).plusDays(12).plusHours(1).plusMinutes(34).plusSeconds(10)
      val result = durationFormatter.durationFromNowWithMoreZeros(futureDate)
      result mustBe "10 years, 1 month, 12 days, 1 hour and 34 minutes"
    }

    "return months, days and hours" in new TestFixture {
      val futureDate = now.plusMonths(2).plusDays(3).plusHours(5).plusMinutes(59).plusSeconds(10)
      val result = durationFormatter.durationFromNowWithMoreZeros(futureDate)
      result mustBe "2 months, 3 days, 5 hours and 59 minutes"
    }

    "return days and hours" in new TestFixture {
      val futureDate = now.plusDays(10).plusHours(12).plusMinutes(30).plusSeconds(10)
      val result = durationFormatter.durationFromNowWithMoreZeros(futureDate)
      result mustBe "10 days, 12 hours and 30 minutes"
    }

    "return only hours" in new TestFixture {
      val futureDate = now.plusHours(2).plusMinutes(30).plusSeconds(10)
      val result = durationFormatter.durationFromNowWithMoreZeros(futureDate)
      result mustBe "0 days, 2 hours and 30 minutes"
    }

    "return 0 days and hours if there is less than 1 day" in new TestFixture {
      val futureDate = now.plusMinutes(4).plusSeconds(10)
      val result = durationFormatter.durationFromNowWithMoreZeros(futureDate)
      result mustBe "0 days, 0 hours and 4 minutes"
    }
  }

  "getExpireTimeLondon" should {
    "return BST time when expirationDate is during BST period" in {
      // BST starts on 26 March at 1:00am UTC, in 2017
      // London is 1 hour ahead of UTC, during BST
      val dateTimeFormatter = new DurationFormatter {
        override def expirationDate = OffsetDateTime.of(java.time.LocalDate.parse("2017-03-26"),
          java.time.LocalTime.parse("01:00"), ZoneOffset.UTC)
      }
      dateTimeFormatter.getExpireTime mustBe "1:00am"
      dateTimeFormatter.getExpireTimeLondon mustBe "2:00am"
    }

    "return GMT time when expirationDate is not during BST period" in {
      val dateTimeFormatter = new DurationFormatter {
        override def expirationDate = OffsetDateTime.of(java.time.LocalDate.parse("2017-03-26"),
          java.time.LocalTime.parse("00:59"), ZoneOffset.UTC)
      }
      dateTimeFormatter.getExpireTime mustBe "12:59am"
      dateTimeFormatter.getExpireTimeLondon mustBe "12:59am"
    }
  }

  "getExpireDateLondon" should {
    "return BST time when expirationDate is during BST period" in {
      // BST starts on 26 March at 1:00am UTC, in 2017
      // London is 1 hour ahead of UTC, during BST
      val dateTimeFormatter = new DurationFormatter {
        override def expirationDate = OffsetDateTime.of(java.time.LocalDate.parse("2017-03-26"),
          java.time.LocalTime.parse("23:59"), ZoneOffset.UTC)
      }
      dateTimeFormatter.getExpireDate mustBe "26 March 2017"
      dateTimeFormatter.getExpireDateLondon mustBe "27 March 2017"
    }

    "return GMT time when expirationDate is not during BST period" in {
      val dateTimeFormatter = new DurationFormatter {
        override def expirationDate = OffsetDateTime.of(java.time.LocalDate.parse("2017-03-26"),
          java.time.LocalTime.parse("00:59"), ZoneOffset.UTC)
      }
      dateTimeFormatter.getExpireDate mustBe "26 March 2017"
      dateTimeFormatter.getExpireDateLondon mustBe "26 March 2017"
    }
  }

  trait TestFixture {
    self =>
    def now = OffsetDateTime.now

    val durationFormatter = new DurationFormatter {
      private[page] override def now = self.now

      override def expirationDate: OffsetDateTime = self.now
    }
  }
}
