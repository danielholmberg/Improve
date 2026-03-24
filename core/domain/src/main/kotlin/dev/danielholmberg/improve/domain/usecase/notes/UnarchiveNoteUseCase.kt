package dev.danielholmberg.improve.domain.usecase.notes

import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.repository.NoteRepository
import javax.inject.Inject

class UnarchiveNoteUseCase @Inject constructor(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(note: Note): Result<Unit> =
        noteRepository.unarchiveNote(note)
}
