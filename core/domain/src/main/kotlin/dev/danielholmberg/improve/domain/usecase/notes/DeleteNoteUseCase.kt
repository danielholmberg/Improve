package dev.danielholmberg.improve.domain.usecase.notes

import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.repository.NoteRepository
import dev.danielholmberg.improve.domain.repository.StorageRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val storageRepository: StorageRepository
) {
    suspend operator fun invoke(note: Note): Result<Unit> {
        note.vipImages.forEach { imageId ->
            storageRepository.deleteImage(note.id, imageId)
        }
        return noteRepository.deleteNote(note.id)
    }
}
