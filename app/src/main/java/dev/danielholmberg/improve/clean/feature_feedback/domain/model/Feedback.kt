package dev.danielholmberg.improve.clean.feature_feedback.domain.model

data class Feedback(
    var uid: String?,
    var feedbackId: String?,
    var subject: String?,
    var message: String?,
    var timestamp: String?
)