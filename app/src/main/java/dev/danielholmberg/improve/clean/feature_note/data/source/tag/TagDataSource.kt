package dev.danielholmberg.improve.clean.feature_note.data.source.tag

import com.google.firebase.database.ChildEventListener
import dev.danielholmberg.improve.clean.feature_note.data.source.entity.TagEntity

interface TagDataSource {
    fun addTag(tagEntity: TagEntity)
    fun deleteTag(tagEntityId: String?)
    fun saveTags(tagEntities: Map<String?, TagEntity>)
    fun addChildEventListener(childEventListener: ChildEventListener)
    fun generateNewTagId(): String?
}