package dev.danielholmberg.improve.data.firebase.dto

data class FeedbackDto(
    val feedback_id: String? = null,
    val uid: String? = null,
    val title: String? = null,
    val feedback: String? = null,
    val timestamp: String? = null
)
