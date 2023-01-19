package dev.danielholmberg.improve.clean.feature_feedback.domain.repository

import dev.danielholmberg.improve.clean.feature_feedback.domain.model.Feedback
import dev.danielholmberg.improve.clean.feature_feedback.domain.util.FeedbackCallback

interface FeedbackRepository {
    fun generateNewFeedbackId(): String?
    fun submit(feedback: Feedback, callback: FeedbackCallback)
}