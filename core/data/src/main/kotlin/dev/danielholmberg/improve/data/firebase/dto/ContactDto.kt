package dev.danielholmberg.improve.data.firebase.dto

data class ContactDto(
    val id: String? = null,
    val name: String? = null,
    val companyId: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val comment: String? = null,
    val timestampAdded: String? = null,
    val timestampUpdated: String? = null
)
