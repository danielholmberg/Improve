package dev.danielholmberg.improve.data.repository

import dev.danielholmberg.improve.data.firebase.datasource.FirebaseFeedbackDataSource
import dev.danielholmberg.improve.data.mapper.FeedbackMapper
import dev.danielholmberg.improve.domain.model.Feedback
import dev.danielholmberg.improve.domain.repository.FeedbackRepository
import javax.inject.Inject

class FeedbackRepositoryImpl @Inject constructor(
    private val feedbackDataSource: FirebaseFeedbackDataSource
) : FeedbackRepository {

    override suspend fun submitFeedback(feedback: Feedback): Result<Unit> =
        feedbackDataSource.submitFeedback(FeedbackMapper.toDto(feedback))
}
