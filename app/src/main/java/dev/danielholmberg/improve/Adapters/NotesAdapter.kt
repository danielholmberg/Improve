package dev.danielholmberg.improve.Adapters

import android.util.Log
import dev.danielholmberg.improve.Improve.Companion.instance
import androidx.recyclerview.widget.RecyclerView
import dev.danielholmberg.improve.ViewHolders.NoteViewHolder
import dev.danielholmberg.improve.Managers.DatabaseManager
import androidx.recyclerview.widget.SortedList
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import dev.danielholmberg.improve.Callbacks.StorageCallback
import android.widget.Toast
import com.google.firebase.database.DatabaseError
import android.view.ViewGroup
import android.view.LayoutInflater
import dev.danielholmberg.improve.Managers.StorageManager
import dev.danielholmberg.improve.Models.Note
import dev.danielholmberg.improve.R
import java.io.File
import java.util.*

class NotesAdapter : RecyclerView.Adapter<NoteViewHolder>() {

    private val databaseManager: DatabaseManager = instance!!.databaseManager
    private val notes: SortedList<Note> =
        SortedList(Note::class.java, object : SortedList.Callback<Note>() {
            override fun compare(o1: Note, o2: Note): Int {
                // Makes sure that the objects has a value for parameter "updated".
                // Those with a value are greater than those without.
                // This issue is only related to Notes created with v1.
                return if (o1.updated == null && o2.updated == null) {
                    0
                } else if (o1.updated != null && o2.updated == null) {
                    1
                } else if (o1.updated == null) {
                    -1
                } else {
                    o1.updated!!.compareTo(o2.updated!!)
                }
            }

            override fun onChanged(position: Int, count: Int) {
                notifyItemChanged(position)
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return (oldItem.title!!.trim { it <= ' ' } == newItem.title!!.trim { it <= ' ' }
                        && oldItem.info!!.trim { it <= ' ' } == newItem.info!!.trim { it <= ' ' }
                        && oldItem.isStared() == newItem.isStared()
                        && oldItem.hasImage() && oldItem.vipImages == newItem.vipImages
                        && oldItem.getTags() == newItem.getTags())
            }

            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id == newItem.id
            }

            override fun onInserted(position: Int, count: Int) {
                notifyItemInserted(position)
            }

            override fun onRemoved(position: Int, count: Int) {
                notifyItemRemoved(position)
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }
        })
    private var notesCopy: List<Note>? = null
    private var filteredNotes: MutableList<Note>? = null

    init {
        initDatabaseListener()
    }

    /**
     * Downloads all notes from the Notes-node and adds a childEventListener to detect changes.
     */
    private fun initDatabaseListener() {
        databaseManager.notesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is triggered when a new child is added
                // to the location to which this listener was added.
                val addedNote = dataSnapshot.getValue(
                    Note::class.java
                )
                if (addedNote != null) {
                    notes.add(addedNote)
                    if (addedNote.hasImage()) {
                        for (imageId in addedNote.vipImages) {
                            val cachedImage = File(
                                instance!!.imageDir,
                                imageId + StorageManager.VIP_IMAGE_SUFFIX
                            )
                            if (cachedImage.exists()) {
                                Log.d(
                                    TAG, "Image for Note: " + addedNote.id +
                                            " exists in Local Filesystem with image id: " + imageId
                                )
                            } else {
                                Log.d(
                                    TAG, "Downloading image from Firebase for Note: " + addedNote.id
                                            + " with image id: " + imageId
                                )
                                instance!!.storageManager.downloadImageToLocalFile(
                                    imageId!!,
                                    object : StorageCallback {
                                        override fun onSuccess(`object`: Any) {}
                                        override fun onFailure(errorMessage: String?) {}
                                        override fun onProgress(progress: Int) {}
                                    })
                            }
                        }
                    }
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is triggered when the data at a child location has changed.
                val updatedNote = dataSnapshot.getValue(
                    Note::class.java
                )
                if (updatedNote != null) {
                    val existingNote = hashMap[updatedNote.id] as Note?
                    var index = notes.size()
                    if (existingNote == null) {
                        notes.add(updatedNote)
                    } else {
                        index = notesList.indexOf(existingNote)
                        notes.updateItemAt(index, updatedNote)
                    }
                    Toast.makeText(instance, "Note updated", Toast.LENGTH_SHORT).show()
                    instance!!.mainActivityRef!!.runOnUiThread { notifyItemChanged(index) }
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // This method is triggered when a child is removed from the location
                // to which this listener was added.
                val removedNote = dataSnapshot.getValue(
                    Note::class.java
                )
                if (removedNote != null) {
                    notes.remove(removedNote)
                } else {
                    Toast.makeText(
                        instance, "Failed to delete note, please try again later",
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
                Log.e(TAG, "Notes ChildEventListener cancelled: $databaseError")
            }
        })
    }

    fun add(note: Note) {
        notes.add(note)
    }

    fun initSearch() {
        notesCopy = notesList
        filteredNotes = ArrayList()
    }

    fun clearFilter() {
        for (note in filteredNotes!!) {
            notes.add(note)
            notifyItemChanged(notes.indexOf(note))
        }
    }

    fun filter(queryText: String) {
        val lowerCaseQuery = queryText.lowercase(Locale.getDefault())
        for (note in notesCopy!!) {
            if (!note.title!!.lowercase(Locale.getDefault()).contains(lowerCaseQuery) &&
                !note.info!!.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
            ) {
                notes.remove(note)
                filteredNotes!!.add(note)
                notifyItemChanged(notes.indexOf(note))
            } else {
                notes.add(note)
                filteredNotes!!.remove(note)
                notifyItemChanged(notes.indexOf(note))
            }
        }
    }

    val notesList: List<Note>
        get() {
            val notesCopy: MutableList<Note> = ArrayList()
            for (i in 0 until notes.size()) {
                notesCopy.add(notes[i])
            }
            return notesCopy
        }
    val hashMap: HashMap<String?, Any>
        get() {
            val hashMap = HashMap<String?, Any>()
            for (i in 0 until notes.size()) {
                val note = notes[i]
                hashMap[note.id] = note
            }
            return hashMap
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(parent.context, view, parent)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bindModelToView(notes[position])
    }

    override fun getItemCount(): Int {
        return notes.size()
    }

    companion object {
        private val TAG = NotesAdapter::class.java.simpleName
    }
}