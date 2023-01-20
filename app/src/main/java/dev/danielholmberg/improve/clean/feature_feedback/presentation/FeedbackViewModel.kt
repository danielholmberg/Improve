package dev.danielholmberg.improve.clean.feature_feedback.presentation

import androidx.lifecycle.ViewModel
import dev.danielholmberg.improve.clean.feature_feedback.domain.use_case.FeedbackUseCases
import dev.danielholmberg.improve.clean.feature_feedback.domain.util.FeedbackCallback

class FeedbackViewModel(
    private val feedbackUseCases: FeedbackUseCases
) : ViewModel() {
    fun submit(subject: String, message: String, callback: FeedbackCallback) {
        feedbackUseCases.submitFeedbackUseCase(subject, message, callback)
    }
}
