package dev.danielholmberg.improve.clean.feature_note.data.repository

import com.google.firebase.database.ChildEventListener
import dev.danielholmberg.improve.clean.feature_note.data.source.tag.TagDataSource
import dev.danielholmberg.improve.clean.feature_note.data.source.entity.TagEntity
import dev.danielholmberg.improve.clean.feature_note.domain.model.Tag
import dev.danielholmberg.improve.clean.feature_note.domain.repository.TagRepository

class TagRepositoryImpl(private val tagDataSource: TagDataSource) : TagRepository {
    override fun addTag(tag: Tag) {
        // Transform into Data Source model
        val tagEntity = TagEntity().fromTag(tag)
        tagDataSource.addTag(tagEntity)
    }

    override fun deleteTag(tagId: String?) {
        tagDataSource.deleteTag(tagId)
    }

    override fun saveTags(tags: HashMap<String?, Tag>) {
        // Transform into Data Source model
        val tagEntitiesMap = tags.entries.associate {
                (id, tag) -> Pair(id, TagEntity().fromTag(tag))
        }
        tagDataSource.saveTags(tagEntitiesMap)
    }

    override fun addChildEventListener(childEventListener: ChildEventListener) {
        tagDataSource.addChildEventListener(childEventListener)
    }

    override fun generateNewTagId(): String? {
        return tagDataSource.generateNewTagId()
    }
}