package dev.danielholmberg.improve.legacy.Models

import dev.danielholmberg.improve.Improve.Companion.instance
import com.google.firebase.database.IgnoreExtraProperties
import android.os.Parcelable
import com.google.firebase.database.Exclude
import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONException
import android.os.Parcel
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

/**
 * Created by Daniel Holmberg on 2018-01-21.
 */
@IgnoreExtraProperties
class Note : Parcelable {
    var id: String? = null
    var title: String? = null
    var info: String? = null
    var stared = false
    var added: String? = null
    var updated: String? = null
    var archived = false
    private var tags: MutableMap<String?, Boolean?> = HashMap()

    // VIP values
    var vipImages = ArrayList<String?>()

    constructor()
    constructor(id: String?) {
        this.id = id
    }

    fun getTags(): Map<String?, Boolean?> {
        return tags
    }

    fun setTags(tags: Map<String?, Boolean?>) {
        val tagsMap: MutableMap<String?, Boolean?> = ConcurrentHashMap(tags)

        // Do not add if Tag has been removed.
        for (tagId in tagsMap.keys) {
            if (instance!!.tagsAdapter!!.getTag(tagId) == null) {
                tagsMap.remove(tagId)
            }
        }
        this.tags = tagsMap
    }

    // Utility functions
    @Exclude
    fun hasImage(): Boolean {
        return vipImages.size > 0
    }

    @Exclude
    fun isStared(): Boolean {
        return stared
    }

    @Exclude
    fun isArchived(): Boolean {
        return archived
    }

    @Exclude
    override fun toString(): String {
        val noteData = JSONObject()
        try {
            noteData.put("id", id)
            noteData.put("archived", archived)
            noteData.put("info", info)
            noteData.put("added", added)
            noteData.put("updated", updated)
            noteData.put("title", title)
            noteData.put("stared", stared)
            if (vipImages.size > 0) noteData.put("vipImages", JSONArray(vipImages))
            if (tags.isNotEmpty()) noteData.put("tags",
                (tags as Map<*, *>?)?.let { JSONObject(it) })
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return noteData.toString()
    }

    @Exclude
    fun addVipImage(vipImageId: String?) {
        vipImages.add(vipImageId)
    }

    @Exclude
    fun removeVipImage(imageId: String?) {
        vipImages.remove(imageId)
    }

    @Exclude
    fun addTag(tagId: String?) {
        tags[tagId] = true
    }

    @Exclude
    fun removeTag(tagId: String?) {
        tags.remove(tagId)
    }

    @Exclude
    fun containsTag(id: String?): Boolean {
        return tags.keys.contains(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(info)
        parcel.writeInt(if (stared) 1 else 0)
        parcel.writeInt(if (archived) 1 else 0)
        parcel.writeString(added)
        parcel.writeString(updated)
        parcel.writeList(vipImages)
        parcel.writeMap(tags)
    }

    constructor(`in`: Parcel) {
        id = `in`.readString()
        title = `in`.readString()
        info = `in`.readString()
        stared = `in`.readInt() != 0
        archived = `in`.readInt() != 0
        added = `in`.readString()
        updated = `in`.readString()
        `in`.readList(vipImages, ArrayList::class.java.classLoader)
        `in`.readMap(tags, HashMap::class.java.classLoader)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val note = other as Note
        return archived == note.archived && stared == note.stared && id == note.id &&
                title == note.title &&
                info == note.info &&
                added == note.added &&
                updated == note.updated &&
                vipImages == note.vipImages &&
                tags == note.tags
    }

    override fun hashCode(): Int {
        return Objects.hash(id, title, info, stared, added, updated, archived, vipImages, tags)
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