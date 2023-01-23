package dev.danielholmberg.improve.clean.feature_note.domain.use_case.note

import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository

class DeleteNoteFromArchiveUseCase(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(noteToDelete: Note) {
        noteRepository.deleteNoteFromArchive(noteToDelete)
    }
}