package dev.danielholmberg.improve.clean.feature_note.presentation.notes.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.api.services.drive.DriveScopes
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.core.GoogleDriveService
import dev.danielholmberg.improve.clean.feature_note.data.source.entity.NoteEntity
import dev.danielholmberg.improve.clean.feature_note.domain.repository.ImageRepository
import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.*
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.image.DownloadImageToLocalFileUseCase
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.image.UploadImagesUseCase
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.note.*
import dev.danielholmberg.improve.clean.feature_note.presentation.create_note.CreateNoteActivity
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.view_model.NotesViewModel

class NotesFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var viewModel: NotesViewModel

    private lateinit var snackBarView: CoordinatorLayout
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var emptyListText: TextView

    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        // 1. Create ViewModel with UseCases and inject necessary repositories
        val noteRepository: NoteRepository = instance!!.noteRepository
        val imageRepository: ImageRepository = instance!!.imageRepository
        viewModel = NotesViewModel(
            noteUseCases = NoteUseCases(
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
            ),
            imageUseCases = ImageUseCases(
                uploadImagesUseCase = UploadImagesUseCase(
                    imageRepository = imageRepository
                ),
                downloadImageToLocalFileUseCase = DownloadImageToLocalFileUseCase(
                    imageRepository = imageRepository
                )
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_notes,
            container, false
        )

        instance!!.mainActivityRef.findViewById<View>(R.id.toolbar_dropshadow).visibility = View.GONE
        snackBarView = view.findViewById(R.id.note_fragment_container)
        notesRecyclerView = view.findViewById<View>(R.id.notes_list) as RecyclerView
        emptyListText = view.findViewById<View>(R.id.empty_notes_list_tv) as TextView
        fab = view.findViewById<View>(R.id.add_note) as FloatingActionButton

        val recyclerLayoutManager = LinearLayoutManager(activity)
        recyclerLayoutManager.reverseLayout = true
        recyclerLayoutManager.stackFromEnd = true

        notesRecyclerView.layoutManager = recyclerLayoutManager
        notesRecyclerView.adapter = instance!!.notesAdapter

        initListScrollListener()
        initListDataChangeListener()

        return view
    }

    private fun initListScrollListener() {
        notesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && fab.isShown) {
                    // Hide the FAB when the user scrolls down.
                    fab.hide()
                }
                if (!recyclerView.canScrollVertically(-1)) {
                    // we have reached the top of the list
                    instance!!.mainActivityRef.findViewById<View>(R.id.toolbar_dropshadow).visibility =
                        View.GONE
                } else {
                    // we are not at the top yet
                    instance!!.mainActivityRef.findViewById<View>(R.id.toolbar_dropshadow).visibility =
                        View.VISIBLE
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Show the FAB when the user has stopped scrolling.
                    fab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        fab.setOnClickListener { createNewNote() }
    }

    private fun initListDataChangeListener() {
        viewModel.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                dataSnapshot.getValue(
                    NoteEntity::class.java
                )?.toNote()?.let { viewModel.handleOnNoteAdded(it) }

                // Update UI to show if list is empty or not
                if (instance!!.notesAdapter!!.itemCount > 0) {
                    notesRecyclerView.visibility = View.VISIBLE
                    emptyListText.visibility = View.GONE

                    // Scroll to the "top" (bottom) to show changed Note.
                    notesRecyclerView.scrollToPosition(instance!!.notesAdapter!!.itemCount - 1)
                } else {
                    notesRecyclerView.visibility = View.GONE
                    emptyListText.visibility = View.VISIBLE
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                dataSnapshot.getValue(
                    NoteEntity::class.java
                )?.toNote()?.let { viewModel.handleOnNoteChanged(it) } ?: return
            }
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                dataSnapshot.getValue(
                    NoteEntity::class.java
                )?.toNote()?.let { viewModel.handleOnNoteRemoved(it, snackBarView) }

                // Update UI to show if list is empty or not
                if (instance!!.notesAdapter!!.itemCount > 0) {
                    notesRecyclerView.visibility = View.VISIBLE
                    emptyListText.visibility = View.GONE
                } else {
                    notesRecyclerView.visibility = View.GONE
                    emptyListText.visibility = View.VISIBLE
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

    private fun createNewNote() {
        val createNewNoteIntent = Intent(instance, CreateNoteActivity::class.java)
        startActivity(createNewNoteIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_notes, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.noteImport -> {
                openFilePicker()
                return true
            }
            R.id.noteSearch -> {
                launchSearchView(item)
                return true
            }
        }
        return false
    }

    private fun launchSearchView(item: MenuItem) {
        val searchView = item.actionView as SearchView
        searchView.queryHint = "Search Note"
        searchView.setOnQueryTextListener(this)
        searchView.setOnCloseListener {
            Log.d(TAG, "Search closed!")
            instance!!.notesAdapter!!.clearFilter()
            true
        }
        val searchEditText =
            searchView.findViewById<View>(androidx.appcompat.R.id.search_src_text) as EditText
        searchEditText.setTextColor(
            ContextCompat.getColor(
                instance!!,
                R.color.search_text_color
            )
        )
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
                instance!!.notesAdapter!!.initSearch()
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                Log.d(TAG, "Search closed!")
                instance!!.notesAdapter!!.clearFilter()
                return true
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_OPEN_FILE && resultData != null) {
                val uri = resultData.data
                uri?.let { openFileFromFilePicker(it) }
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    private fun openFilePicker() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        GoogleSignIn.getClient(instance!!, signInOptions)
        Log.d(TAG, "Opening file picker.")
        val pickerIntent =
            instance!!.googleDriveService.createFilePickerIntent(GoogleDriveService.TYPE_NOTE)

        // The result of the SAF Intent is handled in onActivityResult.
        startActivityForResult(pickerIntent, REQUEST_CODE_OPEN_FILE)
    }

    /**
     * Opens a file from its `uri` returned from the Storage Access Framework file picker
     * initiated by [.openFilePicker].
     */
    private fun openFileFromFilePicker(uri: Uri) {
        Log.d(TAG, "Opening " + uri.path)
        instance!!.googleDriveService.openFileUsingStorageAccessFramework(
            instance!!.contentResolver,
            uri
        )
            .addOnSuccessListener { nameAndContent ->
                val name = nameAndContent.first
                val content = nameAndContent.second
                Log.d(TAG, "Note picked: $name with content: $content")
                try {
                    viewModel.importNoteUseCase(name, content)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to import Note.", e)
                    Toast.makeText(instance, "Failed to import Note", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Unable to open file from picker.", e)
                Toast.makeText(instance, "Failed to import Note", Toast.LENGTH_LONG).show()
            }
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        Log.d(TAG, "Query Submitted: $query")
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        Log.d(TAG, "Query Inserted: $newText")
        instance!!.notesAdapter!!.filter(newText)
        notesRecyclerView.scrollToPosition(instance!!.notesAdapter!!.itemCount - 1)
        return true
    }

    companion object {
        private val TAG = NotesFragment::class.java.simpleName
        private const val REQUEST_CODE_OPEN_FILE = 1
    }
}