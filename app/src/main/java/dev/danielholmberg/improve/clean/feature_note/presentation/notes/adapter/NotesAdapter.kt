package dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import android.view.ViewGroup
import android.view.LayoutInflater
import dev.danielholmberg.improve.BuildConfig
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.view_holder.NoteViewHolder
import java.util.*
import kotlin.collections.ArrayList

class NotesAdapter : RecyclerView.Adapter<NoteViewHolder>() {

    private val notes: SortedList<Note> =
        SortedList(Note::class.java, object : SortedList.Callback<Note>() {
            override fun compare(o1: Note, o2: Note): Int {
                // Makes sure that the objects has a value for parameter "updated".
                // Those with a value are greater than those without.
                // This issue is only related to Notes created with v1.
                return if (o1.updatedAt == null && o2.updatedAt == null) {
                    0
                } else if (o1.updatedAt != null && o2.updatedAt == null) {
                    1
                } else if (o1.updatedAt == null) {
                    -1
                } else {
                    o1.updatedAt!!.compareTo(o2.updatedAt!!)
                }
            }

            override fun onChanged(position: Int, count: Int) {
                notifyItemChanged(position)
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return (oldItem.title!!.trim { it <= ' ' } == newItem.title!!.trim { it <= ' ' }
                        && oldItem.info!!.trim { it <= ' ' } == newItem.info!!.trim { it <= ' ' }
                        && oldItem.isStared == newItem.isStared
                        && oldItem.hasImage() && oldItem.images == newItem.images
                        && oldItem.tags == newItem.tags)
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

    fun add(note: Note) {
        notes.add(note)
        notifyItemChanged(notes.size())
    }

    fun remove(note: Note) {
        notes.remove(note)
    }

    fun contains(note: Note): Boolean {
        val existingNote = hashMap[note.id]
        return existingNote != null
    }

    fun update(note: Note) {
        val index = notesIdList.indexOf(note.id)
        notes.updateItemAt(index, note)
        notifyItemChanged(index)
    }

    fun initSearch() {
        notesCopy = hashMap.values.toList()
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

    private val notesIdList: List<String?>
        get() {
            val notesIdCopy: MutableList<String?> = ArrayList()
            for (i in 0 until notes.size()) {
                notesIdCopy.add(notes[i].id)
            }
            return notesIdCopy
        }

    val hashMap: HashMap<String?, Note>
        get() {
            val hashMap = HashMap<String?, Note>()
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
        private val TAG = BuildConfig.TAG + NotesAdapter::class.java.simpleName
    }
}