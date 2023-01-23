package dev.danielholmberg.improve.clean.feature_note.domain.use_case

import dev.danielholmberg.improve.clean.feature_note.domain.use_case.note.*

data class NoteUseCases(
    val generateNewNoteUseCase: GenerateNewNoteUseCase,
    val addNoteUseCase: AddNoteUseCase,
    val archiveNoteUseCase: ArchiveNoteUseCase,
    val unarchiveNoteUseCase: UnarchiveNoteUseCase,
    val addArchivedNoteUseCase: AddArchivedNoteUseCase,
    val addChildEventListenerUseCase: AddChildEventListenerUseCase,
    val addChildEventListenerForArchiveUseCase: AddChildEventListenerForArchiveUseCase,
    val updateArchivedNoteUseCase: UpdateArchivedNoteUseCase
)