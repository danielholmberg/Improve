package dev.danielholmberg.improve.domain.repository

import dev.danielholmberg.improve.domain.model.Feedback

interface FeedbackRepository {
    suspend fun submitFeedback(feedback: Feedback): Result<Unit>
}
