package dev.danielholmberg.improve.domain.repository

import dev.danielholmberg.improve.domain.model.Company
import dev.danielholmberg.improve.domain.model.Contact
import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.model.Tag

interface DriveRepository {
    suspend fun exportNotes(notes: List<Note>, tags: Map<String, Tag>): Result<String>
    suspend fun exportContact(contact: Contact, company: Company?): Result<String>
}
