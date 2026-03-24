package dev.danielholmberg.improve.domain.usecase.tags

import dev.danielholmberg.improve.domain.model.Tag
import dev.danielholmberg.improve.domain.repository.TagRepository
import javax.inject.Inject

class AddTagUseCase @Inject constructor(private val tagRepository: TagRepository) {
    suspend operator fun invoke(tag: Tag): Result<Unit> {
        if (tag.label.isBlank()) {
            return Result.failure(IllegalArgumentException("Tag label must not be blank"))
        }
        return tagRepository.addTag(tag)
    }
}
