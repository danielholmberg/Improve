package dev.danielholmberg.improve.clean.feature_note.domain.use_case.tag

import dev.danielholmberg.improve.clean.feature_note.domain.model.Tag
import dev.danielholmberg.improve.clean.feature_note.domain.repository.TagRepository

class GenerateNewTagUseCase(
    private val tagRepository: TagRepository
) {
    operator fun invoke(label: String, color: String): Tag {
        val id = tagRepository.generateNewTagId()
        return Tag(id, label, color)
    }
}
