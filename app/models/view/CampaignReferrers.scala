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

package models.view

object CampaignReferrers {
  val list = List(
    ("Social Media (Facebook, Twitter, LinkedIn or Instagram)", false),
    ("Online search (Google)", false),
    ("Friend or family in the Civil Service", false),
    ("Fast Stream University activity (careers service, careers fair, guest lecture, skills session)", false),
    ("Previous internship", false),
    ("Times Top 100", false),
    ("Bright Network", false),
    ("Gradcracker", false),
    ("Zero Gravity", false),
    ("Inservice activity (intranet, networks)", false),
    ("Other", true)
  )
}
