package dev.danielholmberg.improve.clean.feature_contact.data.repository

import com.google.firebase.database.ChildEventListener
import dev.danielholmberg.improve.clean.feature_contact.data.source.contact.ContactDataSource
import dev.danielholmberg.improve.clean.feature_contact.data.source.entity.ContactEntity
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Contact
import dev.danielholmberg.improve.clean.feature_contact.domain.repository.ContactRepository

class ContactRepositoryImpl(
    private val contactDataSource: ContactDataSource
) : ContactRepository {
    override fun generateNewContactIdForCompany(companyId: String): String? {
        return contactDataSource.generateNewContactIdForCompany(companyId)
    }

    override fun addContact(contact: Contact) {
        // Transform into Data Source model
        val contactEntity: ContactEntity = ContactEntity().fromContact(contact)
        contactDataSource.addContact(contactEntity)
    }

    override fun updateContact(oldContact: Contact, updatedContact: Contact) {
        // Transform into Data Source model
        val oldContactEntity: ContactEntity = ContactEntity().fromContact(oldContact)
        val newContactEntity: ContactEntity = ContactEntity().fromContact(updatedContact)
        contactDataSource.updateContact(oldContactEntity, newContactEntity)
    }

    override fun deleteContact(contactToDelete: Contact) {
        // Transform into Data Source model
        val contactEntity: ContactEntity = ContactEntity().fromContact(contactToDelete)
        contactDataSource.deleteContact(contactEntity)
    }

    override fun addChildEventListenerForCompany(
        companyId: String?,
        orderBy: String,
        childEventListener: ChildEventListener
    ) {
        contactDataSource.addChildEventListenerForCompany(companyId, orderBy, childEventListener)
    }
}