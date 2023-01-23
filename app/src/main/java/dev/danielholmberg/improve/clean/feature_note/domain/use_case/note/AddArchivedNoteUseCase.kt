package dev.danielholmberg.improve.clean.feature_note.domain.use_case.note

import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository

class AddArchivedNoteUseCase(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(noteToAdd: Note) {
        noteRepository.addArchivedNote(noteToAdd)
    }
}
