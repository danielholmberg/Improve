package dev.danielholmberg.improve.clean.feature_note.domain.use_case

import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note

class AddNoteUseCase(
    private val repository: NoteRepository
) {

    operator fun invoke(note: Note) {
        repository.addNote(note)
    }
}