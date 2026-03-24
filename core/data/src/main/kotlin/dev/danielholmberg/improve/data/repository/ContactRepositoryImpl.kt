package dev.danielholmberg.improve.data.repository

import dev.danielholmberg.improve.data.firebase.datasource.FirebaseCompanyDataSource
import dev.danielholmberg.improve.data.firebase.datasource.FirebaseContactDataSource
import dev.danielholmberg.improve.data.mapper.ContactMapper
import dev.danielholmberg.improve.domain.model.Contact
import dev.danielholmberg.improve.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    private val contactDataSource: FirebaseContactDataSource,
    private val companyDataSource: FirebaseCompanyDataSource
) : ContactRepository {

    override fun getContacts(): Flow<List<Contact>> = channelFlow {
        val contacts = mutableMapOf<String, Contact>()
        val companyIds = mutableSetOf<String>()

        // Watch for companies appearing so we can attach per-company contact listeners
        companyDataSource.observeCompanies().collect { event ->
            when (event) {
                is FirebaseCompanyDataSource.CompanyEvent.Added -> {
                    val companyId = event.dto.id ?: return@collect
                    if (companyIds.add(companyId)) {
                        launch {
                            contactDataSource.observeContacts(companyId).collect { contactEvent ->
                                when (contactEvent) {
                                    is FirebaseContactDataSource.ContactEvent.Added -> {
                                        val contact = ContactMapper.toDomain(contactEvent.dto)
                                        contacts[contact.id] = contact
                                    }
                                    is FirebaseContactDataSource.ContactEvent.Changed -> {
                                        val contact = ContactMapper.toDomain(contactEvent.dto)
                                        contacts[contact.id] = contact
                                    }
                                    is FirebaseContactDataSource.ContactEvent.Removed -> {
                                        contacts.remove(contactEvent.id)
                                    }
                                }
                                send(contacts.values.toList())
                            }
                        }
                    }
                }
                is FirebaseCompanyDataSource.CompanyEvent.Removed -> {
                    companyIds.remove(event.id)
                    val removed = contacts.values.filter { it.companyId == event.id }
                    removed.forEach { contacts.remove(it.id) }
                    send(contacts.values.toList())
                }
                is FirebaseCompanyDataSource.CompanyEvent.Changed -> Unit
            }
        }
    }

    override suspend fun addContact(contact: Contact): Result<Unit> =
        contactDataSource.addContact(ContactMapper.toDto(contact))

    override suspend fun updateContact(contact: Contact): Result<Unit> =
        contactDataSource.updateContact(ContactMapper.toDto(contact))

    override suspend fun deleteContact(contactId: String, companyId: String): Result<Unit> =
        contactDataSource.deleteContact(contactId, companyId)
}
