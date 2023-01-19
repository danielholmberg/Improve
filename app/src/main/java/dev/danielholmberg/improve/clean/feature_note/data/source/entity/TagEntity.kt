package dev.danielholmberg.improve.clean.feature_note.data.source.entity

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import dev.danielholmberg.improve.clean.feature_note.domain.model.Tag

@IgnoreExtraProperties
data class TagEntity(
    val id: String? = null,
    var label: String? = null,
    var color: String? = null,
    var textColor: String? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "label" to label,
            "color" to color,
            "textColor" to textColor
        )
    }

    @Exclude
    fun fromTag(tag: Tag): TagEntity {
        return TagEntity(
            id = tag.id,
            label = tag.label,
            color = tag.color,
            textColor = tag.textColor
        )
    }

    @Exclude
    fun toTag(): Tag {
        return Tag(
            id = id,
            label = label,
            color = color,
            textColor = textColor
        )
    }
}