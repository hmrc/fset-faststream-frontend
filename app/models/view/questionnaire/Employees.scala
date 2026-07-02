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

package models.view.questionnaire

import models.view.ValidAnswers
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

object Employees extends ValidAnswers {
  val list: List[(String, String, Boolean)] = List(
    ("employee", "Employee", false),
    ("self-with-employees", "Self-employed with employees", false),
    ("self-without-employees", "Self-employed/freelancer without employees", false),
    ("unknown", "I don't know/prefer not to say", false)
  )
  override val values: List[String] = list.map { case (_, value, _) => value }

  def asRadioItems: Seq[RadioItem] = values.map( value =>
    RadioItem(value = Some(value), content = Text(value))
  )
}
