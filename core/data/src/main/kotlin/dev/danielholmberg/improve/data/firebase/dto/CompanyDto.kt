package dev.danielholmberg.improve.data.firebase.dto

data class CompanyDto(
    val id: String? = null,
    val name: String? = null
    // contacts are a sub-collection, loaded separately
)
