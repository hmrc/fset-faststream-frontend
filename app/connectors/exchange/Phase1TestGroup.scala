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

import models.UniqueIdentifier

import java.time.OffsetDateTime
import play.api.libs.json.{Json, OFormat}

// More data is sent by the backend but we only care about the report url
case class PsiTestResult(testReportUrl: Option[String])

object PsiTestResult {
  implicit def testResultFormat: OFormat[PsiTestResult] = Json.format[PsiTestResult]
}

case class PsiTest(inventoryId: String,
                   usedForResults: Boolean,
                   testUrl: String,
                   orderId: UniqueIdentifier,
                   invitationDate: OffsetDateTime,
                   startedDateTime: Option[OffsetDateTime] = None,
                   completedDateTime: Option[OffsetDateTime] = None,
                   resultsReadyToDownload: Boolean = false,
                   testResult: Option[PsiTestResult] = None
) {
  def started: Boolean = startedDateTime.isDefined
  def completed: Boolean = completedDateTime.isDefined
}

object PsiTest {
  implicit def phase1TestFormat: OFormat[PsiTest] = Json.format[PsiTest]
}
