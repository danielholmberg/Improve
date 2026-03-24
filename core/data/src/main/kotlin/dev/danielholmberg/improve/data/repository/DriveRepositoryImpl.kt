package dev.danielholmberg.improve.data.repository

import dev.danielholmberg.improve.data.drive.DriveDataSource
import dev.danielholmberg.improve.domain.model.Company
import dev.danielholmberg.improve.domain.model.Contact
import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.model.Tag
import dev.danielholmberg.improve.domain.repository.DriveRepository
import javax.inject.Inject

class DriveRepositoryImpl @Inject constructor(
    private val driveDataSource: DriveDataSource
) : DriveRepository {

    override suspend fun exportNotes(notes: List<Note>, tags: Map<String, Tag>): Result<String> =
        driveDataSource.exportNotesAsCsv(notes, tags)

    override suspend fun exportContact(contact: Contact, company: Company?): Result<String> =
        driveDataSource.exportContactAsVCard(contact, company)
}
