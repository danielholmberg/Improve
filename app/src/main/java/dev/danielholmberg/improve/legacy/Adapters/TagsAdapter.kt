package dev.danielholmberg.improve.legacy.Adapters

import android.util.Log
import dev.danielholmberg.improve.Improve.Companion.instance
import androidx.recyclerview.widget.RecyclerView
import dev.danielholmberg.improve.legacy.ViewHolders.TagViewHolder
import dev.danielholmberg.improve.legacy.Managers.DatabaseManager
import androidx.recyclerview.widget.SortedList
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import android.widget.Toast
import com.google.firebase.database.DatabaseError
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import dev.danielholmberg.improve.legacy.Models.Note
import dev.danielholmberg.improve.legacy.Models.Tag
import dev.danielholmberg.improve.R
import java.util.HashMap

class TagsAdapter : RecyclerView.Adapter<TagViewHolder>() {

    private val databaseManager: DatabaseManager = instance!!.databaseManager
    private val tags: SortedList<Tag> =
        SortedList(Tag::class.java, object : SortedList.Callback<Tag>() {
            override fun compare(o1: Tag, o2: Tag): Int {
                return o1.id!!.compareTo(o2.id!!)
            }

            override fun onChanged(position: Int, count: Int) {
                notifyItemRangeChanged(position, count)
            }

            override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
                return oldItem.label == newItem.label
                        && oldItem.color == newItem.color
                        && oldItem.textColor == newItem.textColor
            }

            override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
                return oldItem.id == newItem.id
            }

            override fun onInserted(position: Int, count: Int) {
                notifyItemRangeInserted(position, count)
            }

            override fun onRemoved(position: Int, count: Int) {
                notifyItemRangeRemoved(position, count)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }
        })

    private var currentNote: Note? = null
    private var tagView: View? = null
    private var editMode = false

    init {
        initDatabaseListener()
    }

    /**
     * Downloads all tags from the Notes-node and adds a childEventListener to detect changes.
     */
    private fun initDatabaseListener() {
        databaseManager.tagRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is triggered when a new child is added
                // to the location to which this listener was added.
                Log.d(TAG, "Tag OnChildAdded()")
                val addedTag = dataSnapshot.getValue(
                    Tag::class.java
                )
                if (addedTag != null) {
                    tags.add(addedTag)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is triggered when the data at a child location has changed.

                // TODO - Not used at the moment, useful when user can edit Tags.
                Log.d(TAG, "Tag OnChildChanged()")
                val updatedTag = dataSnapshot.getValue(
                    Tag::class.java
                )
                if (updatedTag != null) {
                    tags.add(updatedTag)
                    Toast.makeText(instance, "Tag updated", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // This method is triggered when a child is removed from the location
                // to which this listener was added.

                // TODO - Not used at the moment, useful when user can edit Tags.
                Log.d(TAG, "Tag OnChildRemoved()")
                val removedTag = dataSnapshot.getValue(
                    Tag::class.java
                )
                if (removedTag != null) {
                    tags.remove(removedTag)
                } else {
                    Toast.makeText(
                        instance, "Failed to delete tag, please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                // This method is triggered when a child location's priority changes.
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // This method will be triggered in the event that this listener either failed
                // at the server, or is removed as a result of the security and Firebase rules.
                Log.e(TAG, "Tags ChildEventListener cancelled: $databaseError")
            }
        })
    }

    val hashMap: HashMap<String?, Any>
        get() {
            val hashMap = HashMap<String?, Any>()
            for (i in 0 until tags.size()) {
                val tag = tags[i]
                hashMap[tag.id] = tag
            }
            return hashMap
        }

    fun getTag(tagId: String?): Tag? {
        return hashMap[tagId] as Tag?
    }

    fun addTag(tag: Tag) {
        tags.add(tag)
    }

    fun setCurrentNote(currentNote: Note?) {
        this.currentNote = currentNote
    }

    fun removeCurrentNote() {
        currentNote = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        tagView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tag, parent, false)
        return TagViewHolder(tagView!!)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]
        holder.bindModelToView(tag)
        holder.setEditMode(editMode)
        if (currentNote != null) {
            tagView!!.setOnClickListener {
                if (currentNote!!.containsTag(tag.id)) {
                    currentNote!!.removeTag(tag.id)
                    holder.setTagStatusOnNote(false)
                } else {
                    currentNote!!.addTag(tag.id)
                    holder.setTagStatusOnNote(true)
                }
            }
            if (currentNote!!.containsTag(tag.id)) {
                holder.setTagStatusOnNote(true)
            } else {
                holder.setTagStatusOnNote(false)
            }
        }
    }

    override fun getItemCount(): Int {
        return tags.size()
    }

    fun setEditMode(editMode: Boolean) {
        this.editMode = editMode
        notifyDataSetChanged()
    }

    companion object {
        private val TAG = TagsAdapter::class.java.simpleName
    }
}