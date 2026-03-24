package dev.danielholmberg.improve.domain.usecase.notes

import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.model.Tag
import dev.danielholmberg.improve.domain.repository.DriveRepository
import javax.inject.Inject

class ExportNotesUseCase @Inject constructor(private val driveRepository: DriveRepository) {
    suspend operator fun invoke(notes: List<Note>, tags: Map<String, Tag>): Result<String> =
        driveRepository.exportNotes(notes, tags)
}
