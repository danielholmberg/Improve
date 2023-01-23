package dev.danielholmberg.improve.clean.feature_note.domain.use_case.note

import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note

class AddNoteUseCase(
    private val noteRepository: NoteRepository
) {

    operator fun invoke(note: Note) {
        noteRepository.addNote(note)
    }
}