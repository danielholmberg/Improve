package dev.danielholmberg.improve.domain.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val isAnonymous: Boolean = false,
    val isVip: Boolean = false
)
