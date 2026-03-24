package dev.danielholmberg.improve.domain.model

data class Contact(
    val id: String = "",
    val name: String = "",
    val companyId: String = "",
    val email: String = "",
    val phone: String = "",
    val comment: String = "",
    val timestampAdded: String = "",
    val timestampUpdated: String = ""
)
