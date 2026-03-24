package dev.danielholmberg.improve.domain.usecase.contacts

import dev.danielholmberg.improve.domain.model.Contact
import dev.danielholmberg.improve.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetContactsUseCase @Inject constructor(private val contactRepository: ContactRepository) {
    operator fun invoke(): Flow<List<Contact>> =
        contactRepository.getContacts().map { contacts ->
            contacts.sortedBy { it.name }
        }
}
