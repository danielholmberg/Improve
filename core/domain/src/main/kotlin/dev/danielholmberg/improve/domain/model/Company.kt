package dev.danielholmberg.improve.domain.model

data class Company(
    val id: String = "",
    val name: String = "",
    val contacts: Map<String, Boolean> = emptyMap()
)
