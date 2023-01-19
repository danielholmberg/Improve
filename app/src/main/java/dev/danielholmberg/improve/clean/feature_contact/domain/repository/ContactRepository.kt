package dev.danielholmberg.improve.clean.feature_contact.domain.repository

import com.google.firebase.database.ChildEventListener
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Contact


interface ContactRepository {
    fun generateNewContactIdForCompany(companyId: String): String?
    fun addContact(contact: Contact)
    fun updateContact(oldContact: Contact, updatedContact: Contact)
    fun deleteContact(contactToDelete: Contact)
    fun addChildEventListenerForCompany(
        companyId: String?,
        orderBy: String,
        childEventListener: ChildEventListener
    )
}