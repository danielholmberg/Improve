package dev.danielholmberg.improve.domain.model

data class Note(
    val id: String = "",
    val title: String = "",
    val info: String = "",
    val isStarred: Boolean = false,
    val isArchived: Boolean = false,
    val added: String = "",
    val updated: String = "",
    val tags: Map<String, Boolean> = emptyMap(),
    val vipImages: List<String> = emptyList()
)
