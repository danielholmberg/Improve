package dev.danielholmberg.improve.domain.model

data class Feedback(
    val feedbackId: String = "",
    val uid: String = "",
    val title: String = "",
    val feedback: String = "",
    val timestamp: String = ""
)
