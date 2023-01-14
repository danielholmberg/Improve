package dev.danielholmberg.improve.Adapters

import android.util.Log
import dev.danielholmberg.improve.Improve.Companion.instance
import androidx.recyclerview.widget.RecyclerView
import dev.danielholmberg.improve.ViewHolders.ArchivedNoteViewHolder
import dev.danielholmberg.improve.Managers.DatabaseManager
import androidx.recyclerview.widget.SortedList
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import android.widget.Toast
import com.google.firebase.database.DatabaseError
import android.view.ViewGroup
import android.view.LayoutInflater
import dev.danielholmberg.improve.Models.Note
import dev.danielholmberg.improve.R
import java.util.*

class ArchivedNotesAdapter : RecyclerView.Adapter<ArchivedNoteViewHolder>() {

    private val databaseManager: DatabaseManager = instance!!.databaseManager
    private val archivedNotes: SortedList<Note> =
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
                notifyItemRangeChanged(position, count)
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.title!!.trim { it <= ' ' } == newItem.title!!.trim { it <= ' ' }
                        && oldItem.info!!.trim { it <= ' ' } == newItem.info!!.trim { it <= ' ' }
                        && oldItem.getTags().size != newItem.getTags().size
            }

            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
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

    private var archivedNotesCopy: List<Note>? = null
    private var filteredArchivedNotes: MutableList<Note>? = null

    init {
        initDatabaseListener()
    }

    /**
     * Downloads all archivedNotes from the Notes-node and adds a childEventListener to detect changes.
     */
    private fun initDatabaseListener() {
        databaseManager.archivedNotesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is triggered when a new child is added
                // to the location to which this listener was added.
                val archivedNote = dataSnapshot.getValue(
                    Note::class.java
                )
                if (archivedNote != null) {
                    archivedNotes.add(archivedNote)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is triggered when the data at a child location has changed.
                val updatedNote = dataSnapshot.getValue(
                    Note::class.java
                )
                if (updatedNote != null) {
                    val existingNote = hashMap[updatedNote.id] as Note?
                    if (existingNote == null) {
                        archivedNotes.add(updatedNote)
                    } else {
                        archivedNotes.updateItemAt(
                            archivedNotesList.indexOf(existingNote),
                            updatedNote
                        )
                    }
                    Toast.makeText(instance, "Archived note updated", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // This method is triggered when a child is removed from the location
                // to which this listener was added.
                val removedNote = dataSnapshot.getValue(
                    Note::class.java
                )
                if (removedNote != null) {
                    archivedNotes.remove(removedNote)
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
                Log.e(TAG, "ArchivedNotes ChildEventListener cancelled: $databaseError")
            }
        })
    }

    fun initSearch() {
        archivedNotesCopy = archivedNotesList
        filteredArchivedNotes = ArrayList()
    }

    fun clearFilter() {
        for (note in filteredArchivedNotes!!) {
            archivedNotes.add(note)
            notifyItemChanged(archivedNotes.indexOf(note))
        }
    }

    fun filter(queryText: String) {
        val lowerCaseQuery = queryText.lowercase(Locale.getDefault())
        for (note in archivedNotesCopy!!) {
            if (!note.title!!.lowercase(Locale.getDefault()).contains(lowerCaseQuery) &&
                !note.info!!.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
            ) {
                archivedNotes.remove(note)
                filteredArchivedNotes!!.add(note)
                notifyItemChanged(archivedNotes.indexOf(note))
            } else {
                archivedNotes.add(note)
                filteredArchivedNotes!!.remove(note)
                notifyItemChanged(archivedNotes.indexOf(note))
            }
        }
    }

    val archivedNotesList: List<Note>
        get() {
            val archivedNotesCopy: MutableList<Note> = ArrayList()
            for (i in 0 until archivedNotes.size()) {
                archivedNotesCopy.add(archivedNotes[i])
            }
            return archivedNotesCopy
        }
    val hashMap: HashMap<String?, Any>
        get() {
            val hashMap = HashMap<String?, Any>()
            for (i in 0 until archivedNotes.size()) {
                val note = archivedNotes[i]
                hashMap[note.id] = note
            }
            return hashMap
        }

    override fun getItemCount(): Int {
        return archivedNotes.size()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivedNoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_archived_note, parent, false)
        return ArchivedNoteViewHolder(parent.context, view, parent)
    }

    override fun onBindViewHolder(holder: ArchivedNoteViewHolder, position: Int) {
        holder.bindModelToView(archivedNotes[position])
    }

    companion object {
        private val TAG = ArchivedNotesAdapter::class.java.simpleName
    }
}