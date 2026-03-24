package dev.danielholmberg.improve.domain.usecase.contacts

import dev.danielholmberg.improve.domain.model.Company
import dev.danielholmberg.improve.domain.model.Contact
import dev.danielholmberg.improve.domain.repository.DriveRepository
import javax.inject.Inject

class ExportContactUseCase @Inject constructor(private val driveRepository: DriveRepository) {
    suspend operator fun invoke(contact: Contact, company: Company?): Result<String> =
        driveRepository.exportContact(contact, company)
}
