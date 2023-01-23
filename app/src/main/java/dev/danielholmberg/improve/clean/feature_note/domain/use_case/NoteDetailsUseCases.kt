package dev.danielholmberg.improve.clean.feature_note.domain.use_case

import dev.danielholmberg.improve.clean.feature_note.domain.use_case.note.*

data class NoteDetailsUseCases(
    val generateNewNoteUseCase: GenerateNewNoteUseCase,
    val addNoteUseCase: AddNoteUseCase,
    val updateNoteUseCase: UpdateNoteUseCase,
    val deleteNoteUseCase: DeleteNoteUseCase,
    val deleteNoteFromArchiveUseCase: DeleteNoteFromArchiveUseCase,
    val updateArchivedNoteUseCase: UpdateArchivedNoteUseCase,
    val archiveNoteUseCase: ArchiveNoteUseCase,
    val unarchiveNoteUseCase: UnarchiveNoteUseCase
)
