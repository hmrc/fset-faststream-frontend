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

import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

import scala.collection.immutable.ListMap

object Ethnicities {
  val map: Map[String, List[(String, Boolean)]] = ListMap(
    "White" -> List(
      ("English, Welsh, Scottish, Northern Irish or British", false),
      ("Irish", false),
      ("Gypsy or Irish Traveller", false),
      ("Roma", false),
      ("Any other White background", true)
    ),
    "Mixed or Multiple ethnic groups" -> List(
      ("White and Black Caribbean", false),
      ("White and Black African", false),
      ("White and Asian", false),
      ("Any other Mixed or Multiple ethnic background", true)
    ),
    "Asian or Asian British" -> List(
      ("Indian", false),
      ("Pakistani", false),
      ("Bangladeshi", false),
      ("Chinese", false),
      ("Any other Asian background", true)
    ),
    "Black, Black British, Caribbean or African" -> List(
      ("African", false),
      ("Caribbean", false),
      ("Any other Black, Black British, or Caribbean background", true)
    ),
    "Other ethnic group" -> List(
      ("Arab", false),
      ("Any other ethnic group", true)
    )
  )

  val asSelectItems: Seq[SelectItem] = Seq(SelectItem(value = None, text = "-- Select one --")) ++
    Seq(
      "English, Welsh, Scottish, Northern Irish or British",
      "Irish",
      "Gypsy or Irish Traveller",
      "Roma",
      "Any other White background",
      "White and Black Caribbean",
      "White and Black African",
      "White and Asian",
      "Any other Mixed or Multiple ethnic background",
      "Indian",
      "Pakistani",
      "Bangladeshi",
      "Chinese",
      "Any other Asian background",
      "African",
      "Caribbean",
      "Any other Black, Black British, or Caribbean background",
      "Arab",
      "Any other ethnic group",
      "I don't know/prefer not to say" // This is an additional item to the list
  ).map(ethnicity => SelectItem(value = Some(ethnicity), text = ethnicity))
}
