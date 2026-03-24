package dev.danielholmberg.improve.domain.usecase.notes

import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.repository.NoteRepository
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(note: Note): Result<Unit> {
        if (note.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Note title must not be blank"))
        }
        return noteRepository.updateNote(note)
    }
}
