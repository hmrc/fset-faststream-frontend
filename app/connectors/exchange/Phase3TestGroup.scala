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

import play.api.libs.json.{Json, OFormat}

import java.time.OffsetDateTime

// This file has not been migrated from Joda Time to Java time, because it is used by Phase3/ video tests stage and this
// stage is disabled at this moment and can not be tested. We could have quickly migrated that to Java time but without
// proper testing, it is not recommended. Once we enable Phase3/ video tests in the future we could proceed to migrate
// to Java time and then do all the manual testing required.
case class ReviewSectionCriteriaRequest(`type`: String, score: Option[Double])

object ReviewSectionCriteriaRequest {
  implicit val reviewCriteriaFormat: OFormat[ReviewSectionCriteriaRequest] = Json.format[ReviewSectionCriteriaRequest]
}

case class ReviewSectionQuestionRequest(id: Int, reviewCriteria1: ReviewSectionCriteriaRequest,
                                        reviewCriteria2: ReviewSectionCriteriaRequest)

object ReviewSectionQuestionRequest {
  implicit val reviewSectionReviewerQuestion: OFormat[ReviewSectionQuestionRequest] = Json.format[ReviewSectionQuestionRequest]
}

case class ReviewSectionReviewerRequest(name: String, email: String, comment: Option[String],
  question1: ReviewSectionQuestionRequest,
  question2: ReviewSectionQuestionRequest,
  question3: ReviewSectionQuestionRequest,
  question4: ReviewSectionQuestionRequest,
  question5: ReviewSectionQuestionRequest,
  question6: ReviewSectionQuestionRequest,
  question7: ReviewSectionQuestionRequest,
  question8: ReviewSectionQuestionRequest)

object ReviewSectionReviewerRequest {
  implicit val reviewSectionReviewerFormat: OFormat[ReviewSectionReviewerRequest] = Json.format[ReviewSectionReviewerRequest]
}

case class ReviewSectionTotalAverageRequest(`type`: String, scoreText: String, scoreValue: Double)

case class ReviewSectionReviewersRequest(
  reviewer1: ReviewSectionReviewerRequest,
  reviewer2: Option[ReviewSectionReviewerRequest],
  reviewer3: Option[ReviewSectionReviewerRequest]
)

object ReviewSectionReviewersRequest {
  implicit val reviewSectionReviewersFormat: OFormat[ReviewSectionReviewersRequest] = Json.format[ReviewSectionReviewersRequest]
}

object ReviewSectionTotalAverageRequest {
  implicit val reviewSectionTotalAverageFormat: OFormat[ReviewSectionTotalAverageRequest] = Json.format[ReviewSectionTotalAverageRequest]
}

case class ReviewSectionRequest(
  totalAverage: ReviewSectionTotalAverageRequest,
  reviewers: ReviewSectionReviewersRequest
)

object ReviewSectionRequest {
  implicit val reviewSectionFormat: OFormat[ReviewSectionRequest] = Json.format[ReviewSectionRequest]
}

case class ReviewedCallbackRequest(
  received: OffsetDateTime,
  reviews: ReviewSectionRequest) {

  val reviewers = reviews.reviewers
  val latestReviewer = reviewers.reviewer3.getOrElse(reviewers.reviewer2.getOrElse(reviewers.reviewer1))

  def calculateTotalScore: Double = {
    calculateReviewCriteria1Score + calculateReviewCriteria2Score
  }

  def calculateReviewCriteria1Score: Double = {
    aggregateScoresForAllQuestion(question => question.reviewCriteria1)
  }

  def calculateReviewCriteria2Score: Double = {
    aggregateScoresForAllQuestion(question => question.reviewCriteria2)
  }

  private def aggregateScoresForAllQuestion(scoreExtractor: ReviewSectionQuestionRequest => ReviewSectionCriteriaRequest) = {
    (
      BigDecimal(scoreExtractor(latestReviewer.question1).score.getOrElse(0.0)) +
      BigDecimal(scoreExtractor(latestReviewer.question2).score.getOrElse(0.0)) +
      BigDecimal(scoreExtractor(latestReviewer.question3).score.getOrElse(0.0)) +
      BigDecimal(scoreExtractor(latestReviewer.question4).score.getOrElse(0.0)) +
      BigDecimal(scoreExtractor(latestReviewer.question5).score.getOrElse(0.0)) +
      BigDecimal(scoreExtractor(latestReviewer.question6).score.getOrElse(0.0)) +
      BigDecimal(scoreExtractor(latestReviewer.question7).score.getOrElse(0.0)) +
      BigDecimal(scoreExtractor(latestReviewer.question8).score.getOrElse(0.0))
    ).toDouble
  }

  def allQuestionsReviewed: Boolean = {
    val questions = List(latestReviewer.question1, latestReviewer.question2, latestReviewer.question3, latestReviewer.question4,
      latestReviewer.question5, latestReviewer.question6, latestReviewer.question7, latestReviewer.question8)
    questions.nonEmpty && questions.forall(ques => ques.reviewCriteria1.score.isDefined && ques.reviewCriteria2.score.isDefined)
  }
}

object ReviewedCallbackRequest {
  implicit val reviewedCallbackFormat: OFormat[ReviewedCallbackRequest] = Json.format[ReviewedCallbackRequest]
}

case class LaunchpadTestCallbacks(
  reviewed: List[ReviewedCallbackRequest] = Nil) {
  def getLatestReviewed: Option[ReviewedCallbackRequest] =
    reviewed.sortWith { (r1, r2) => r1.received.isAfter(r2.received) }.headOption
}

object LaunchpadTestCallbacks {
  implicit val launchpadTestCallbacksFormat: OFormat[LaunchpadTestCallbacks] = Json.format[LaunchpadTestCallbacks]
}

case class Phase3Test(usedForResults: Boolean,
                      testUrl: String,
                      token: String,
                      invitationDate: OffsetDateTime,
                      startedDateTime: Option[OffsetDateTime] = None,
                      completedDateTime: Option[OffsetDateTime] = None,
  callbacks: LaunchpadTestCallbacks) {
  def started: Boolean = startedDateTime.isDefined
  def completed: Boolean = completedDateTime.isDefined
}

object Phase3Test {
  implicit def phase3TestFormat: OFormat[Phase3Test] = Json.format[Phase3Test]
}

case class Phase3TestGroup(expirationDate: OffsetDateTime, tests: List[Phase3Test],
                           evaluation: Option[PassmarkEvaluation] = None) {
  def activeTests: Seq[Phase3Test] = tests.filter(_.usedForResults)
}

object Phase3TestGroup {
  implicit val phase3TestGroupFormat: OFormat[Phase3TestGroup] = Json.format[Phase3TestGroup]
}
