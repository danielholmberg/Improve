package dev.danielholmberg.improve.data.firebase.dto

data class NoteDto(
    val id: String? = null,
    val title: String? = null,
    val info: String? = null,
    val stared: Boolean = false,
    val archived: Boolean = false,
    val added: String? = null,
    val updated: String? = null,
    val tags: Map<String, Boolean>? = null,
    val vipImages: ArrayList<String>? = null
)
