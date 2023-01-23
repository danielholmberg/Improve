package dev.danielholmberg.improve.clean.feature_note.domain.use_case.tag

import dev.danielholmberg.improve.clean.feature_note.domain.model.Tag
import dev.danielholmberg.improve.clean.feature_note.domain.repository.TagRepository

class AddTagUseCase(
    private val tagRepository: TagRepository
) {
    operator fun invoke(tag: Tag) {
        tagRepository.addTag(tag)
    }
}
