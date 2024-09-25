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

import connectors.exchange.candidatescores.{AssessmentScoresAllExercises, ExerciseAverageResult}

case class ExerciseFeedback(exerciseName: String, competencyFeedback: Seq[CompetencyFeedback])
case class CompetencyFeedback(competencyName: String, feedback: String)

case class AssessmentFeedbackPage(
  exerciseFeedbackData: Seq[ExerciseFeedback],
  finalFeedback: String,
  exerciseAverageResults: ExerciseAverageResult,
  candidateName: String
) {
  def formatScore(score: Double): String = "%.2f".format(score)

  override def toString: String =
    s"exerciseFeedbackData=$exerciseFeedbackData," +
      s"finalFeedback=$finalFeedback," +
      s"exerciseAverageResults=$exerciseAverageResults," +
      s"candidateName=$candidateName"
}

case object AssessmentFeedbackPage {
  val relatesCompetency = "Relates"
  val thinksCompetency = "Thinks"
  val strivesCompetency = "Strives"
  val adaptsCompetency = "Adapts"

  def apply(assessmentScores: AssessmentScoresAllExercises,
            exerciseAverageResults: ExerciseAverageResult, candidateName: String): AssessmentFeedbackPage = {
    val exercise1 = ExerciseFeedback("Written advice exercise",
      Seq(
        CompetencyFeedback(thinksCompetency,
          assessmentScores.exercise1.flatMap { s => s.thinksFeedback }.getOrElse("")),
        CompetencyFeedback(relatesCompetency,
          assessmentScores.exercise1.flatMap{ s => s.relatesFeedback}.getOrElse("")),
        CompetencyFeedback(strivesCompetency,
          assessmentScores.exercise1.flatMap{ s => s.strivesFeedback}.getOrElse(""))
      )
    )
    val exercise2 = ExerciseFeedback("Stakeholder communication exercise",
      Seq(
        CompetencyFeedback(thinksCompetency,
          assessmentScores.exercise2.flatMap{ s => s.thinksFeedback}.getOrElse("")),
        CompetencyFeedback(relatesCompetency,
          assessmentScores.exercise2.flatMap{ s => s.relatesFeedback}.getOrElse("")),
        CompetencyFeedback(adaptsCompetency,
          assessmentScores.exercise2.flatMap{ s => s.adaptsFeedback}.getOrElse("")),
        CompetencyFeedback(strivesCompetency,
          assessmentScores.exercise2.flatMap{ s => s.strivesFeedback}.getOrElse(""))
      )
    )
    val exercise3 = ExerciseFeedback("Personal development conversation",
      Seq(
        CompetencyFeedback(relatesCompetency,
          assessmentScores.exercise3.flatMap { s => s.relatesFeedback }.getOrElse("")),
        CompetencyFeedback(adaptsCompetency,
          assessmentScores.exercise3.flatMap{ s => s.adaptsFeedback}.getOrElse("")),
        CompetencyFeedback(strivesCompetency,
          assessmentScores.exercise3.flatMap{ s => s.strivesFeedback}.getOrElse(""))
      )
    )
    val finalFeedback = assessmentScores.finalFeedback.map { s => s.feedback }.getOrElse("")
    AssessmentFeedbackPage(Seq(exercise1, exercise2, exercise3),
      finalFeedback, exerciseAverageResults, candidateName
    )
  }
}
