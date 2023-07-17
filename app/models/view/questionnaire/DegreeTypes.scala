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

object DegreeTypes {
  val list = List(
    // Display text, value, display hidden panel
    ("BSc/MSc/Eng", "BSc/MSc/Eng", false),
    ("BA/MA/LLB/MBA", "BA/MA/LLB/MBA", false),
    ("PhD/DPhil/MRes - STEM focus", "PhD/DPhil/MRes - STEM", false),
    ("PhD/DPhil/MRes - non-STEM focus", "PhD/DPhil/MRes - non-STEM", false),
    ("Other", "Other", true)
  )

  val validDegreeTypes = list.map { case (_, value, _) => value }
}
