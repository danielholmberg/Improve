package dev.danielholmberg.improve.clean.feature_note.domain.use_case.note

import com.google.firebase.database.ChildEventListener
import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository

class AddChildEventListenerUseCase(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(childEventListener: ChildEventListener) {
        noteRepository.addChildEventListener(childEventListener)
    }
}