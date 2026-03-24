package dev.danielholmberg.improve.domain.usecase.tags

import dev.danielholmberg.improve.domain.repository.TagRepository
import javax.inject.Inject

class DeleteTagUseCase @Inject constructor(private val tagRepository: TagRepository) {
    suspend operator fun invoke(tagId: String): Result<Unit> =
        tagRepository.deleteTag(tagId)
}
