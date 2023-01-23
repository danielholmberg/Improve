package dev.danielholmberg.improve.clean.feature_note.domain.use_case.note

import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository

class ArchiveNoteUseCase(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(noteToArchive: Note) {
        noteRepository.archiveNote(noteToArchive)
    }
}
