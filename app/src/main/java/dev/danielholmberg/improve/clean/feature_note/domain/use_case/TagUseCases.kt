package dev.danielholmberg.improve.clean.feature_note.domain.use_case

import dev.danielholmberg.improve.clean.feature_note.domain.use_case.tag.AddTagUseCase
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.tag.GenerateNewTagUseCase

data class TagUseCases(
    val generateNewTagUseCase: GenerateNewTagUseCase,
    val addTagUseCase: AddTagUseCase
)