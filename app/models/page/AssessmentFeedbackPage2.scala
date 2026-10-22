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

import connectors.exchange.candidatescores.ExerciseAverageResult

// As of campaign 2026/27, the assessors no longer enter candidate specific feedback.
// The feedback in the report screen is now generic. The old version of this page is still being kept
// until we remove the template that uses it.
// IMPORTANT: when removing this class, copy the contents into the original class so we keep the history
case class AssessmentFeedbackPage2(
  exerciseAverageResults: ExerciseAverageResult,
  candidateName: String
) {
  def formatScore(score: Double): String = "%.2f".format(score)

  override def toString: String =
    s"exerciseAverageResults=$exerciseAverageResults," +
      s"candidateName=$candidateName"
}
