package dev.danielholmberg.improve.clean.feature_note.presentation.notes.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.feature_note.data.source.entity.NoteEntity
import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.*
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.note.*
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.view_model.ArchivedNotesViewModel

class ArchivedNotesFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var viewModel: ArchivedNotesViewModel

    private lateinit var snackBarView: CoordinatorLayout
    private lateinit var archivedNotesRecyclerView: RecyclerView
    private lateinit var emptyListText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        // 1. Create ViewModel with UseCases and inject necessary repositories
        val noteRepository: NoteRepository = instance!!.noteRepository
        viewModel = ArchivedNotesViewModel(
            noteUseCase = NoteUseCases(
                generateNewNoteUseCase = GenerateNewNoteUseCase(
                    noteRepository = noteRepository
                ),
                addNoteUseCase = AddNoteUseCase(
                    noteRepository = noteRepository
                ),
                archiveNoteUseCase = ArchiveNoteUseCase(
                    noteRepository = noteRepository,
                ),
                unarchiveNoteUseCase = UnarchiveNoteUseCase(
                    noteRepository = noteRepository
                ),
                addArchivedNoteUseCase = AddArchivedNoteUseCase(
                    noteRepository = noteRepository
                ),
                addChildEventListenerUseCase = AddChildEventListenerUseCase(
                    noteRepository = noteRepository
                ),
                addChildEventListenerForArchiveUseCase = AddChildEventListenerForArchiveUseCase(
                    noteRepository = noteRepository
                ),
                updateArchivedNoteUseCase = UpdateArchivedNoteUseCase(
                    noteRepository = noteRepository
                )
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_archived_notes,
            container, false
        )

        instance!!.mainActivityRef.findViewById<View>(R.id.toolbar_dropshadow).visibility = View.GONE
        snackBarView = view.findViewById(R.id.archivednote_fragment_container)
        archivedNotesRecyclerView = view.findViewById<View>(R.id.archived_notes_list) as RecyclerView
        emptyListText = view.findViewById<View>(R.id.empty_archive_list_tv) as TextView

        val recyclerLayoutManager = LinearLayoutManager(activity)
        recyclerLayoutManager.reverseLayout = true
        recyclerLayoutManager.stackFromEnd = true

        archivedNotesRecyclerView.layoutManager = recyclerLayoutManager
        archivedNotesRecyclerView.adapter = instance!!.archivedNotesAdapter

        initListScrollListener()
        initListDataChangeListener()

        return view
    }

    private fun initListScrollListener() {
        archivedNotesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(-1)) {
                    // we have reached the top of the list
                    Log.d(TAG, "Reached the top!")
                    instance!!.mainActivityRef.findViewById<View>(R.id.toolbar_dropshadow).visibility =
                        View.GONE
                } else {
                    // we are not at the top yet
                    Log.d(TAG, "not at top yet!")
                    instance!!.mainActivityRef.findViewById<View>(R.id.toolbar_dropshadow).visibility =
                        View.VISIBLE
                }
            }
        })
    }

    private fun initListDataChangeListener() {
        viewModel.addChildEventListenerForArchive(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                dataSnapshot.getValue(
                    NoteEntity::class.java
                )?.toNote()?.let { viewModel.handleOnArchiveNoteAdded(it) }

                // Update UI to show if list is empty or not
                if (instance!!.archivedNotesAdapter!!.itemCount > 0) {
                    archivedNotesRecyclerView.visibility = View.VISIBLE
                    emptyListText.visibility = View.GONE
                } else {
                    archivedNotesRecyclerView.visibility = View.GONE
                    emptyListText.visibility = View.VISIBLE
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                dataSnapshot.getValue(
                    NoteEntity::class.java
                )?.toNote()?.let { viewModel.handleOnArchivedNoteUpdated(it) } ?: return
            }
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                dataSnapshot.getValue(
                    NoteEntity::class.java
                )?.toNote()?.let { viewModel.handleOnArchivedNoteRemoved(it, snackBarView) }

                // Update UI to show if list is empty or not
                if (instance!!.archivedNotesAdapter!!.itemCount > 0) {
                    archivedNotesRecyclerView.visibility = View.VISIBLE
                    emptyListText.visibility = View.GONE
                } else {
                    archivedNotesRecyclerView.visibility = View.GONE
                    emptyListText.visibility = View.VISIBLE
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_archived_notes, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search_archived_note -> {
                launchSearchView(item)
                return true
            }
        }
        return false
    }

    private fun launchSearchView(item: MenuItem) {
        val searchView = item.actionView as SearchView
        searchView.queryHint = "Search Archived Note"
        searchView.setOnQueryTextListener(this)
        searchView.setOnCloseListener {
            Log.d(TAG, "Search closed!")
            instance!!.archivedNotesAdapter!!.clearFilter()
            true
        }
        val searchEditText =
            searchView.findViewById<View>(androidx.appcompat.R.id.search_src_text) as EditText
        searchEditText.setTextColor(ContextCompat.getColor(instance!!, R.color.search_text_color))
        searchEditText.setHintTextColor(
            ContextCompat.getColor(
                instance!!,
                R.color.search_hint_color
            )
        )
        searchEditText.isCursorVisible = false
        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                Log.d(TAG, "Search opened!")
                instance!!.archivedNotesAdapter!!.initSearch()
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                Log.d(TAG, "Search closed!")
                instance!!.archivedNotesAdapter!!.clearFilter()
                return true
            }
        })
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        Log.d(TAG, "Query Submitted: $query")
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        Log.d(TAG, "Query Inserted: $newText")
        instance!!.archivedNotesAdapter!!.filter(newText)
        archivedNotesRecyclerView.scrollToPosition(instance!!.archivedNotesAdapter!!.itemCount - 1)
        return true
    }

    companion object {
        private val TAG = ArchivedNotesFragment::class.java.simpleName
    }
}