package dev.danielholmberg.improve.domain.repository

import dev.danielholmberg.improve.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun getContacts(): Flow<List<Contact>>
    suspend fun addContact(contact: Contact): Result<Unit>
    suspend fun updateContact(contact: Contact): Result<Unit>
    suspend fun deleteContact(contactId: String, companyId: String): Result<Unit>
}
