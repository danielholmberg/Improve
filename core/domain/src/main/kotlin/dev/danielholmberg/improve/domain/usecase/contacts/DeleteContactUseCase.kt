package dev.danielholmberg.improve.domain.usecase.contacts

import dev.danielholmberg.improve.domain.repository.ContactRepository
import javax.inject.Inject

class DeleteContactUseCase @Inject constructor(private val contactRepository: ContactRepository) {
    suspend operator fun invoke(contactId: String, companyId: String): Result<Unit> =
        contactRepository.deleteContact(contactId, companyId)
}
