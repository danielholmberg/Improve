package dev.danielholmberg.improve.clean.feature_note.domain.model

import dev.danielholmberg.improve.clean.Improve.Companion.instance
import com.google.firebase.database.IgnoreExtraProperties
import android.os.Parcelable
import com.google.firebase.database.Exclude
import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONException
import android.os.Parcel
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@IgnoreExtraProperties
data class Note(
    var id: String? = null,
    var title: String? = null,
    var info: String? = null,
    var isStared: Boolean = false,
    var addedAt: String? = null,
    var updatedAt: String? = null,
    var isArchived: Boolean = false,
    var tags: MutableMap<String, Boolean> = HashMap(),
    var images: ArrayList<String> = ArrayList(),
) : Parcelable {

    constructor(id: String?) : this() {
        this.id = id
    }

    constructor(`in`: Parcel) : this() {
        id = `in`.readString()
        title = `in`.readString()
        info = `in`.readString()
        isStared = `in`.readInt() != 0
        isArchived = `in`.readInt() != 0
        addedAt = `in`.readString()
        updatedAt = `in`.readString()
        `in`.readList(images, String::class.java.classLoader)
        `in`.readMap(tags, Boolean::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(info)
        parcel.writeInt(if (isStared) 1 else 0)
        parcel.writeInt(if (isArchived) 1 else 0)
        parcel.writeString(addedAt)
        parcel.writeString(updatedAt)
        parcel.writeList(images)
        parcel.writeMap(tags)
    }

    @JvmName("setTags1")
    fun setTags(tags: Map<String, Boolean>) {
        val tagsMap: MutableMap<String, Boolean> = ConcurrentHashMap(tags)

        // Do not add if Tag has been removed.
        for (tagId in tagsMap.keys) {
            if (instance!!.tagsAdapter!!.getTag(tagId) == null) {
                tagsMap.remove(tagId)
            }
        }
        this.tags = tagsMap
    }

    @Exclude
    fun addTag(tagId: String) {
        tags[tagId] = true
    }

    @Exclude
    fun removeTag(tagId: String) {
        tags.remove(tagId)
    }

    @Exclude
    fun containsTag(id: String?): Boolean {
        return tags.keys.contains(id)
    }

    @Exclude
    fun hasImage(): Boolean {
        return images.size > 0
    }

    @Exclude
    fun addImage(imageId: String) {
        images.add(imageId)
    }

    @Exclude
    fun removeImage(imageId: String?) {
        images.remove(imageId)
    }

    @Exclude
    fun toggleIsStared() {
        isStared = !isStared
    }

    @Exclude
    override fun toString(): String {
        val noteData = JSONObject()
        try {
            noteData.put("id", id)
            noteData.put("archived", isArchived)
            noteData.put("info", info)
            noteData.put("added", addedAt)
            noteData.put("updated", updatedAt)
            noteData.put("title", title)
            noteData.put("stared", isStared)
            if (images.size > 0) noteData.put("images", JSONArray(images))
            if (tags.isNotEmpty()) noteData.put("tags",
                (tags as Map<*, *>?)?.let { JSONObject(it) })
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return noteData.toString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val note = other as Note
        return isArchived == note.isArchived && isStared == note.isStared && id == note.id &&
                title == note.title &&
                info == note.info &&
                addedAt == note.addedAt &&
                updatedAt == note.updatedAt &&
                images == note.images &&
                tags == note.tags
    }

    override fun hashCode(): Int {
        return Objects.hash(id, title, info, isStared, addedAt, updatedAt, isArchived, images, tags)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Note> = object : Parcelable.Creator<Note> {
            override fun createFromParcel(`in`: Parcel): Note {
                return Note(`in`)
            }

            override fun newArray(size: Int): Array<Note?> {
                return arrayOfNulls(size)
            }
        }
    }
}