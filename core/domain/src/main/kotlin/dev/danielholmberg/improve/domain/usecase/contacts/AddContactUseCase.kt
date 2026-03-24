package dev.danielholmberg.improve.domain.usecase.contacts

import dev.danielholmberg.improve.domain.model.Contact
import dev.danielholmberg.improve.domain.repository.ContactRepository
import javax.inject.Inject

class AddContactUseCase @Inject constructor(private val contactRepository: ContactRepository) {
    suspend operator fun invoke(contact: Contact): Result<Unit> {
        if (contact.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Contact name must not be blank"))
        }
        return contactRepository.addContact(contact)
    }
}
