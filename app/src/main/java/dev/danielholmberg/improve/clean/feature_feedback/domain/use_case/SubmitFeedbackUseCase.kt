package dev.danielholmberg.improve.clean.feature_feedback.domain.use_case

import dev.danielholmberg.improve.clean.feature_authentication.domain.repository.AuthRepository
import dev.danielholmberg.improve.clean.feature_feedback.domain.model.Feedback
import dev.danielholmberg.improve.clean.feature_feedback.domain.repository.FeedbackRepository
import dev.danielholmberg.improve.clean.feature_feedback.domain.util.FeedbackCallback
import java.text.DateFormat
import java.util.*

class SubmitFeedbackUseCase(
    private val feedbackRepository: FeedbackRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(subject: String, message: String, callback: FeedbackCallback) {
        val userId = authRepository.getCurrentUserId()
        val feedbackId = feedbackRepository.generateNewFeedbackId()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val timestamp = DateFormat.getDateTimeInstance().format(calendar.time)

        val feedback = Feedback(userId, feedbackId, subject, message, timestamp)
        feedbackRepository.submit(feedback, callback)
    }
}
