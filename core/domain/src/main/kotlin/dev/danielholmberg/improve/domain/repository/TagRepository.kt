package dev.danielholmberg.improve.domain.repository

import dev.danielholmberg.improve.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getTags(): Flow<List<Tag>>
    suspend fun addTag(tag: Tag): Result<Unit>
    suspend fun deleteTag(tagId: String): Result<Unit>
}
