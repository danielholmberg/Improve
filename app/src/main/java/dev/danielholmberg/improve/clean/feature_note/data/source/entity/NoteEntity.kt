package dev.danielholmberg.improve.clean.feature_note.data.source.entity

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note

@IgnoreExtraProperties
data class NoteEntity(
    val id: String? = null,
    var title: String? = null,
    var info: String? = null,
    var stared: Boolean = false,
    var added: String? = null,
    var updated: String? = null,
    var archived: Boolean = false,
    var tags: MutableMap<String?, Boolean?> = HashMap(),
    var vipImages: ArrayList<String?> = ArrayList(),
) {
    @Exclude
    fun fromNote(note: Note): NoteEntity {
        return NoteEntity(
            id = note.id,
            title = note.title,
            info = note.info,
            stared = note.isStared,
            archived = note.isArchived,
            added = note.addedAt,
            updated = note.updatedAt,
            tags = note.tags,
            vipImages = note.images,
        )
    }

    @Exclude
    fun toNote(): Note {
        return Note(
            id = id,
            title = title,
            info = info,
            isStared = stared,
            isArchived = archived,
            addedAt = added,
            updatedAt = updated,
            tags = tags,
            images = vipImages
        )
    }
}