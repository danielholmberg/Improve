package dev.danielholmberg.improve.domain.usecase.tags

import dev.danielholmberg.improve.domain.model.Tag
import dev.danielholmberg.improve.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(private val tagRepository: TagRepository) {
    operator fun invoke(): Flow<List<Tag>> = tagRepository.getTags()
}
