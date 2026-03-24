package dev.danielholmberg.improve.data.repository

import dev.danielholmberg.improve.data.firebase.datasource.FirebaseTagDataSource
import dev.danielholmberg.improve.data.mapper.TagMapper
import dev.danielholmberg.improve.domain.model.Tag
import dev.danielholmberg.improve.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagDataSource: FirebaseTagDataSource
) : TagRepository {

    override fun getTags(): Flow<List<Tag>> = flow {
        val tags = mutableMapOf<String, Tag>()
        tagDataSource.observeTags().collect { event ->
            when (event) {
                is FirebaseTagDataSource.TagEvent.Added -> {
                    val tag = TagMapper.toDomain(event.dto)
                    tags[tag.id] = tag
                }
                is FirebaseTagDataSource.TagEvent.Changed -> {
                    val tag = TagMapper.toDomain(event.dto)
                    tags[tag.id] = tag
                }
                is FirebaseTagDataSource.TagEvent.Removed -> {
                    tags.remove(event.id)
                }
            }
            emit(tags.values.toList())
        }
    }

    override suspend fun addTag(tag: Tag): Result<Unit> =
        tagDataSource.addTag(TagMapper.toDto(tag))

    override suspend fun deleteTag(tagId: String): Result<Unit> =
        tagDataSource.deleteTag(tagId)
}
