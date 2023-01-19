package dev.danielholmberg.improve.clean.feature_note.domain.repository

import com.google.firebase.database.ChildEventListener
import dev.danielholmberg.improve.clean.feature_note.domain.model.Tag


interface TagRepository {
    fun addTag(tag: Tag)
    fun deleteTag(tagId: String?)
    fun saveTags(tags: HashMap<String?, Tag>)
    fun addChildEventListener(childEventListener: ChildEventListener)
    fun generateNewTagId(): String?
}
