/*
 * Copyright 2026 HM Revenue & Customs
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

package helpers

import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

object PasswordRequirements {
  def apply(): HtmlContent = HtmlContent(
    s"""
       |<p class="govuk-body">You must have:</p>
       |<ul id="passwordRequirements" class="govuk-list govuk-list--bullet">
       |  <li id="includesUppercase">uppercase letters</li>
       |  <li id="includesLowercase">lowercase letters</li>
       |  <li id="includesNumber">a number</li>
       |  <li id="includes9Characters">at least 9 characters</li>
       |</ul>
       |""".stripMargin
  )
}
