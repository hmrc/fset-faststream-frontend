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

package models

import connectors.exchange.{AssessmentCentre, Fsb, ProgressResponse}
import play.api.libs.json.{Format, Json, OFormat}

import scala.language.implicitConversions

case class Phase1TestProgress(
    phase1TestsInvited: Boolean = false,
    phase1TestsFirstReminder: Boolean = false,
    phase1TestsSecondReminder: Boolean = false,
    phase1TestsStarted: Boolean = false,
    phase1TestsCompleted: Boolean = false,
    phase1TestsExpired: Boolean = false,
    phase1TestsResultsReady: Boolean = false,
    phase1TestsResultsReceived: Boolean = false,
    phase1TestsPassed: Boolean = false,
    phase1TestsPassedNotified: Boolean = false, // Only applicable for Sdip candidates
    phase1TestsFailed: Boolean = false,
    sdipFSFailed: Boolean = false,
    sdipFSFailedNotified: Boolean = false,
    sdipFSSuccessful: Boolean = false,
    sdipFSSuccessfulNotified: Boolean = false
)

case class Phase2TestProgress(
    phase2TestsInvited: Boolean = false,
    phase2TestsFirstReminder: Boolean = false,
    phase2TestsSecondReminder: Boolean = false,
    phase2TestsStarted: Boolean = false,
    phase2TestsCompleted: Boolean = false,
    phase2TestsExpired: Boolean = false,
    phase2TestsResultsReady: Boolean = false,
    phase2TestsResultsReceived: Boolean = false,
    phase2TestsPassed: Boolean = false,
    phase2TestsFailed: Boolean = false
)

case class Phase3TestProgress(
    phase3TestsInvited: Boolean = false,
    phase3TestsFirstReminder: Boolean = false,
    phase3TestsSecondReminder: Boolean = false,
    phase3TestsStarted: Boolean = false,
    phase3TestsCompleted: Boolean = false,
    phase3TestsExpired: Boolean = false,
    phase3TestsResultsReceived: Boolean = false,
    phase3TestsPassedWithAmber: Boolean = false,
    phase3TestsPassed: Boolean = false,
    phase3TestsPassedNotified: Boolean = false,
    phase3TestsFailed: Boolean = false,
    phase3TestsFailedNotified: Boolean = false,
    phase3TestsFailedSdipAmber: Boolean = false,
    phase3TestsFailedSdipGreen: Boolean = false
)

case class SiftProgress(
    siftEntered: Boolean = false,
    siftTestInvited: Boolean = false,
    siftTestStarted: Boolean = false,
    siftTestCompleted: Boolean = false,
    siftReady: Boolean = false,
    siftCompleted: Boolean = false,
    siftExpired: Boolean = false,
    sdipFailedAtSift: Boolean = false,
    failedAtSift: Boolean = false,
    failedAtSiftNotified: Boolean = false
)

case class JobOfferProgress(
    eligible: Boolean = false,
    eligibleNotified: Boolean = false
)

case class Progress(
    personalDetails: Boolean = false,
    schemePreferences: Boolean = false,
    locationPreferences: Boolean = false,
    assistanceDetails: Boolean = false,
    preview: Boolean = false,
    startedQuestionnaire: Boolean = false,
    diversityQuestionnaire: Boolean = false,
    educationQuestionnaire: Boolean = false,
    occupationQuestionnaire: Boolean = false,
    submitted: Boolean = false,
    withdrawn: Boolean = false,
    jobOffer: JobOfferProgress = JobOfferProgress(),
    phase1TestProgress: Phase1TestProgress = Phase1TestProgress(),
    phase2TestProgress: Phase2TestProgress = Phase2TestProgress(),
    phase3TestProgress: Phase3TestProgress = Phase3TestProgress(),
    siftProgress: SiftProgress = SiftProgress(),
    exported: Boolean = false,
    updateExported: Boolean = false,
    assessmentCentre: AssessmentCentre = AssessmentCentre(),
    fsb: Fsb = Fsb()
) {
  override def toString: String =
    s"personalDetails=$personalDetails," +
      s"schemePreferences=$schemePreferences," +
      s"locationPreferences=$locationPreferences," +
      s"assistanceDetails=$assistanceDetails," +
      s"preview=$preview," +
      s"startedQuestionnaire=$startedQuestionnaire," +
      s"diversityQuestionnaire=$diversityQuestionnaire," +
      s"educationQuestionnaire=$educationQuestionnaire," +
      s"occupationQuestionnaire=$occupationQuestionnaire," +
      s"submitted=$submitted," +
      s"withdrawn=$withdrawn," +
      s"jobOffer=$jobOffer," +
      s"phase1TestProgress=$phase1TestProgress," +
      s"phase2TestProgress=$Phase2TestProgress," +
      s"phase3TestProgress=$Phase3TestProgress," +
      s"siftProgress=$SiftProgress," +
      s"exported=$exported," +
      s"updateExported=$updateExported," +
      s"assessmentCentre=$AssessmentCentre," +
      s"fsb=$fsb"
}

object Progress {
  implicit val assessmentCentreFormat: OFormat[AssessmentCentre] = Json.format[AssessmentCentre]
  implicit val fsbFormat: OFormat[Fsb] = Json.format[Fsb]
  implicit val eligibleForJobOfferProgressFormat: OFormat[JobOfferProgress] = Json.format[JobOfferProgress]
  implicit val phase1TestProgressFormat: OFormat[Phase1TestProgress] = Json.format[Phase1TestProgress]
  implicit val phase2TestProgressFormat: OFormat[Phase2TestProgress] = Json.format[Phase2TestProgress]
  implicit val phase3TestProgressFormat: OFormat[Phase3TestProgress] = Json.format[Phase3TestProgress]
  implicit val siftProgressFormat: OFormat[SiftProgress] = Json.format[SiftProgress]
  implicit val progressFormat: Format[Progress] = Json.format[Progress]

  // scalastyle:off method.length
  implicit def fromProgressRespToAppProgress(
      progressResponse: ProgressResponse): Progress =
    Progress(
      personalDetails = progressResponse.personalDetails,
      schemePreferences = progressResponse.schemePreferences,
      locationPreferences = progressResponse.locationPreferences,
      assistanceDetails = progressResponse.assistanceDetails,
      preview = progressResponse.preview,
      startedQuestionnaire =
        progressResponse.questionnaire.contains("start_questionnaire"),
      diversityQuestionnaire =
        progressResponse.questionnaire.contains("diversity_questionnaire"),
      educationQuestionnaire =
        progressResponse.questionnaire.contains("education_questionnaire"),
      occupationQuestionnaire =
        progressResponse.questionnaire.contains("occupation_questionnaire"),
      submitted = progressResponse.submitted,
      withdrawn = progressResponse.withdrawn,
      jobOffer = JobOfferProgress(
        eligible = progressResponse.eligibleForJobOffer.eligible,
        eligibleNotified = progressResponse.eligibleForJobOffer.eligibleNotified
      ),
      phase1TestProgress = Phase1TestProgress(
        phase1TestsInvited =
          progressResponse.phase1ProgressResponse.phase1TestsInvited,
        phase1TestsStarted =
          progressResponse.phase1ProgressResponse.phase1TestsStarted,
        phase1TestsFirstReminder =
          progressResponse.phase1ProgressResponse.phase1TestsFirstReminder,
        phase1TestsSecondReminder =
          progressResponse.phase1ProgressResponse.phase1TestsSecondReminder,
        phase1TestsCompleted =
          progressResponse.phase1ProgressResponse.phase1TestsCompleted,
        phase1TestsExpired =
          progressResponse.phase1ProgressResponse.phase1TestsExpired,
        phase1TestsResultsReady =
          progressResponse.phase1ProgressResponse.phase1TestsResultsReady,
        phase1TestsResultsReceived =
          progressResponse.phase1ProgressResponse.phase1TestsResultsReceived,
        phase1TestsPassed =
          progressResponse.phase1ProgressResponse.phase1TestsPassed,
        phase1TestsPassedNotified =
          progressResponse.phase1ProgressResponse.phase1TestsSuccessNotified,
        phase1TestsFailed =
          progressResponse.phase1ProgressResponse.phase1TestsFailed,
        sdipFSFailed = progressResponse.phase1ProgressResponse.sdipFSFailed,
        sdipFSFailedNotified =
          progressResponse.phase1ProgressResponse.sdipFSFailedNotified,
        sdipFSSuccessful =
          progressResponse.phase1ProgressResponse.sdipFSSuccessful
      ),
      phase2TestProgress = Phase2TestProgress(
        phase2TestsInvited =
          progressResponse.phase2ProgressResponse.phase2TestsInvited,
        phase2TestsStarted =
          progressResponse.phase2ProgressResponse.phase2TestsStarted,
        phase2TestsFirstReminder =
          progressResponse.phase2ProgressResponse.phase2TestsFirstReminder,
        phase2TestsSecondReminder =
          progressResponse.phase2ProgressResponse.phase2TestsSecondReminder,
        phase2TestsCompleted =
          progressResponse.phase2ProgressResponse.phase2TestsCompleted,
        phase2TestsExpired =
          progressResponse.phase2ProgressResponse.phase2TestsExpired,
        phase2TestsResultsReady =
          progressResponse.phase2ProgressResponse.phase2TestsResultsReady,
        phase2TestsResultsReceived =
          progressResponse.phase2ProgressResponse.phase2TestsResultsReceived,
        phase2TestsPassed =
          progressResponse.phase2ProgressResponse.phase2TestsPassed,
        phase2TestsFailed =
          progressResponse.phase2ProgressResponse.phase2TestsFailed
      ),
      phase3TestProgress = Phase3TestProgress(
        phase3TestsInvited =
          progressResponse.phase3ProgressResponse.phase3TestsInvited,
        phase3TestsFirstReminder =
          progressResponse.phase3ProgressResponse.phase3TestsFirstReminder,
        phase3TestsSecondReminder =
          progressResponse.phase3ProgressResponse.phase3TestsSecondReminder,
        phase3TestsStarted =
          progressResponse.phase3ProgressResponse.phase3TestsStarted,
        phase3TestsCompleted =
          progressResponse.phase3ProgressResponse.phase3TestsCompleted,
        phase3TestsExpired =
          progressResponse.phase3ProgressResponse.phase3TestsExpired,
        phase3TestsResultsReceived =
          progressResponse.phase3ProgressResponse.phase3TestsResultsReceived,
        phase3TestsPassedWithAmber =
          progressResponse.phase3ProgressResponse.phase3TestsPassedWithAmber,
        phase3TestsPassed =
          progressResponse.phase3ProgressResponse.phase3TestsPassed,
        phase3TestsPassedNotified =
          progressResponse.phase3ProgressResponse.phase3TestsPassedNotified,
        phase3TestsFailed =
          progressResponse.phase3ProgressResponse.phase3TestsFailed,
        phase3TestsFailedNotified =
          progressResponse.phase3ProgressResponse.phase3TestsFailedNotified,
        phase3TestsFailedSdipAmber =
          progressResponse.phase3ProgressResponse.phase3TestsFailedSdipAmber,
        phase3TestsFailedSdipGreen =
          progressResponse.phase3ProgressResponse.phase3TestsFailedSdipGreen
      ),
      siftProgress = SiftProgress(
        siftEntered = progressResponse.siftProgressResponse.siftEntered,
        siftTestInvited = progressResponse.siftProgressResponse.siftTestInvited,
        siftTestStarted = progressResponse.siftProgressResponse.siftTestStarted,
        siftTestCompleted = progressResponse.siftProgressResponse.siftTestCompleted,
        siftReady = progressResponse.siftProgressResponse.siftReady,
        siftCompleted = progressResponse.siftProgressResponse.siftCompleted,
        siftExpired = progressResponse.siftProgressResponse.siftExpired,
        sdipFailedAtSift =
          progressResponse.siftProgressResponse.sdipFailedAtSift,
        failedAtSift = progressResponse.siftProgressResponse.failedAtSift,
        failedAtSiftNotified =
          progressResponse.siftProgressResponse.failedAtSiftNotified
      ),
      exported = progressResponse.exported,
      updateExported = progressResponse.updateExported,
      assessmentCentre = progressResponse.assessmentCentre,
      fsb = progressResponse.fsb
    )
  // scalastyle:on method.length
}
