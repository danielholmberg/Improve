package dev.danielholmberg.improve.domain.usecase.feedback

import dev.danielholmberg.improve.domain.model.Feedback
import dev.danielholmberg.improve.domain.repository.FeedbackRepository
import javax.inject.Inject

class SubmitFeedbackUseCase @Inject constructor(private val feedbackRepository: FeedbackRepository) {
    suspend operator fun invoke(feedback: Feedback): Result<Unit> {
        if (feedback.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Feedback title must not be blank"))
        }
        if (feedback.feedback.isBlank()) {
            return Result.failure(IllegalArgumentException("Feedback body must not be blank"))
        }
        return feedbackRepository.submitFeedback(feedback)
    }
}
