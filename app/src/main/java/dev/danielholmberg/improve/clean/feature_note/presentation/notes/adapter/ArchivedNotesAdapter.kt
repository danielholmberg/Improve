package dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import android.view.ViewGroup
import android.view.LayoutInflater
import dev.danielholmberg.improve.BuildConfig
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.view_holder.ArchivedNoteViewHolder
import java.util.*

class ArchivedNotesAdapter : RecyclerView.Adapter<ArchivedNoteViewHolder>() {

    private val archivedNotes: SortedList<Note> =
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
                notifyItemRangeChanged(position, count)
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.title!!.trim { it <= ' ' } == newItem.title!!.trim { it <= ' ' }
                        && oldItem.info!!.trim { it <= ' ' } == newItem.info!!.trim { it <= ' ' }
                        && oldItem.tags.size != newItem.tags.size
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

    fun add(note: Note) {
        archivedNotes.add(note)
        notifyItemChanged(archivedNotes.size())
    }

    fun remove(note: Note) {
        archivedNotes.remove(note)
    }

    fun contains(note: Note): Boolean {
        val existingNote = hashMap[note.id]
        return existingNote != null
    }

    fun update(note: Note) {
        val index = notesIdList.indexOf(note.id)
        archivedNotes.updateItemAt(index, note)
        notifyItemChanged(index)
    }

    fun initSearch() {
        archivedNotesCopy = hashMap.values.toList()
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

    private val notesIdList: List<String?>
        get() {
            val notesIdCopy: MutableList<String?> = ArrayList()
            for (i in 0 until archivedNotes.size()) {
                notesIdCopy.add(archivedNotes[i].id)
            }
            return notesIdCopy
        }

    val hashMap: HashMap<String?, Note>
        get() {
            val hashMap = HashMap<String?, Note>()
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
        private val TAG = BuildConfig.TAG + ArchivedNotesAdapter::class.java.simpleName
    }
}