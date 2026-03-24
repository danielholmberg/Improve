package dev.danielholmberg.improve.domain.usecase.notes

import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(private val noteRepository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> =
        noteRepository.getNotes().map { notes ->
            notes.sortedWith(
                compareByDescending<Note> { it.isStarred }
                    .thenByDescending { it.updated }
            )
        }
}
