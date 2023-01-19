package dev.danielholmberg.improve.clean.feature_contact.data.source.contact

import com.google.firebase.database.ChildEventListener
import dev.danielholmberg.improve.clean.feature_contact.data.source.entity.ContactEntity

interface ContactDataSource {
    fun generateNewContactIdForCompany(companyId: String): String?
    fun addContact(contactEntity: ContactEntity)
    fun updateContact(oldContactEntity: ContactEntity, newContactEntity: ContactEntity)
    fun deleteContact(contactEntity: ContactEntity)
    fun addChildEventListenerForCompany(
        companyId: String?,
        orderBy: String,
        childEventListener: ChildEventListener
    )
}
