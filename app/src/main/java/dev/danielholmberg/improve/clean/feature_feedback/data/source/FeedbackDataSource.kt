package dev.danielholmberg.improve.clean.feature_feedback.data.source

import dev.danielholmberg.improve.clean.feature_feedback.data.entity.FeedbackEntity
import dev.danielholmberg.improve.clean.feature_feedback.domain.util.FeedbackCallback

interface FeedbackDataSource {
    fun generateNewFeedbackId(): String?
    fun submit(feedbackEntity: FeedbackEntity, callback: FeedbackCallback)
}
