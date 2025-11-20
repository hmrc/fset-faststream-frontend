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

import mappings.Mappings.*
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Singleton

object OnboardQuestionsForm {
  case class Data(niNumber: String)
}

@Singleton
class OnboardQuestionsForm {

  /**
   * Nino validation meets all the following criteria:
   * First 2 characters must not be combinations of GB, NK, TN or ZZ (the term combinations covers both GB and BG etc.)
   * First character must not be D,F,I,Q,U or V.
   * Second character must not be D, F, I, O, Q, U or V.
   * The next 6 characters are numbers.
   * Final character can be A, B, C, D.
   */
  val form: Form[OnboardQuestionsForm.Data] = Form(
    mapping(
      "niNumber" -> (nonEmptyTrimmedText("error.niNumber.wrong.format", 9) verifying
        ("error.niNumber.wrong.format", value => value.matches("^(?!GB|BG|NK|KN|TN|NT|ZZ)[^DFIQUV][^DFIOQUV][0-9]{6}[ABCD]$")))
    )(OnboardQuestionsForm.Data.apply)(f => Some(f.niNumber))
  )
}
