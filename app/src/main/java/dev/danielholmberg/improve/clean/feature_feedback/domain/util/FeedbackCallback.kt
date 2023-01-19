package dev.danielholmberg.improve.clean.feature_feedback.domain.util

interface FeedbackCallback {
    fun onSuccess()
    fun onFailure(errorMessage: String?)
}