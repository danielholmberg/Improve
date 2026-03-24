package dev.danielholmberg.improve.domain.usecase.notes

import dev.danielholmberg.improve.domain.repository.NoteRepository
import javax.inject.Inject

class StarNoteUseCase @Inject constructor(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(noteId: String, starred: Boolean): Result<Unit> =
        noteRepository.starNote(noteId, starred)
}
