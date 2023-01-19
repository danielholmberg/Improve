package dev.danielholmberg.improve.clean.feature_contact.data.source.contact

import android.util.Log
import com.google.firebase.database.ChildEventListener
import dev.danielholmberg.improve.clean.feature_contact.data.source.entity.ContactEntity
import dev.danielholmberg.improve.clean.feature_contact.domain.repository.CompanyRepository

class ContactDataSourceImpl(
    private val companyRepository: CompanyRepository
) : ContactDataSource {
    override fun generateNewContactIdForCompany(companyId: String): String? {
        return companyRepository.getCompaniesRef().child(companyId).child(CONTACTS_REF).push().key
    }

    override fun addContact(contactEntity: ContactEntity) {
        Log.d(TAG, "addContact: " + contactEntity.id)
        companyRepository.getCompaniesRef().child(contactEntity.companyId!!).child(CONTACTS_REF)
            .child(contactEntity.id!!).setValue(contactEntity) { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(TAG, "Failed to add new Contact-id to Company-ref: $databaseError")
                }
            }
    }

    override fun updateContact(oldContactEntity: ContactEntity, newContactEntity: ContactEntity) {
        companyRepository.getCompaniesRef().child(newContactEntity.companyId!!).child(CONTACTS_REF)
            .child(newContactEntity.id!!)
            .setValue(newContactEntity) { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(
                        TAG,
                        "Failed to update Contact ($newContactEntity.id}) from Firebase: $databaseError"
                    )
                }
            }
        if (newContactEntity.companyId != oldContactEntity.companyId) {
            companyRepository.getCompaniesRef().child(oldContactEntity.companyId!!)
                .child(CONTACTS_REF).child(oldContactEntity.id!!)
                .removeValue()
        }
    }

    override fun deleteContact(contactEntity: ContactEntity) {
        companyRepository.getCompaniesRef().child(contactEntity.companyId!!).child(CONTACTS_REF)
            .child(contactEntity.id!!).removeValue { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(
                        TAG,
                        "Failed to delete Contact ($contactEntity.id}) from Firebase: $databaseError"
                    )
                }
            }
    }

    override fun addChildEventListenerForCompany(
        companyId: String?,
        orderBy: String,
        childEventListener: ChildEventListener
    ) {
        companyRepository.getCompaniesRef().child(companyId!!).child(CONTACTS_REF)
            .addChildEventListener(childEventListener)
    }

    companion object {
        private val TAG = ContactDataSourceImpl::class.java.simpleName
        private const val CONTACTS_REF = "contacts"
    }
}