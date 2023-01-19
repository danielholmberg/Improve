package dev.danielholmberg.improve.clean.feature_feedback.data.repository

import dev.danielholmberg.improve.clean.feature_feedback.data.entity.FeedbackEntity
import dev.danielholmberg.improve.clean.feature_feedback.data.source.FeedbackDataSource
import dev.danielholmberg.improve.clean.feature_feedback.domain.model.Feedback
import dev.danielholmberg.improve.clean.feature_feedback.domain.repository.FeedbackRepository
import dev.danielholmberg.improve.clean.feature_feedback.domain.util.FeedbackCallback

class FeedbackRepositoryImpl(
    private val feedbackDataSource: FeedbackDataSource
) : FeedbackRepository {
    override fun generateNewFeedbackId(): String? {
        return feedbackDataSource.generateNewFeedbackId()
    }

    override fun submit(feedback: Feedback, callback: FeedbackCallback) {
        // Transform into Data Source model
        val entity: FeedbackEntity = FeedbackEntity().fromFeedback(feedback)
        feedbackDataSource.submit(entity, callback)
    }
}