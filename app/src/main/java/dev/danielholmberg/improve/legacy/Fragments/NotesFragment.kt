package dev.danielholmberg.improve.legacy.Fragments

import dev.danielholmberg.improve.Improve.Companion.instance
import dev.danielholmberg.improve.legacy.Managers.DatabaseManager
import dev.danielholmberg.improve.legacy.Services.DriveServiceHelper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Bundle
import dev.danielholmberg.improve.R
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseError
import android.content.Intent
import dev.danielholmberg.improve.legacy.Activities.AddNoteActivity
import android.widget.EditText
import android.app.Activity
import android.net.Uri
import android.util.Log
import android.view.*
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.services.drive.DriveScopes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.Scope
import com.google.gson.Gson
import com.google.gson.JsonParser
import dev.danielholmberg.improve.legacy.Models.Note
import java.lang.Exception

class NotesFragment : Fragment(), SearchView.OnQueryTextListener {

    private var databaseManager: DatabaseManager? = null
    private var mDriveServiceHelper: DriveServiceHelper? = null
    private var snackBarView: CoordinatorLayout? = null
    private var notesRecyclerView: RecyclerView? = null
    private var recyclerLayoutManager: LinearLayoutManager? = null
    private var emptyListText: TextView? = null
    private var fab: FloatingActionButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance!!.notesFragmentRef = this
        databaseManager = instance!!.databaseManager
        mDriveServiceHelper = instance!!.driveServiceHelper
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_notes,
            container, false
        )
        instance!!.mainActivityRef!!.findViewById<View>(R.id.toolbar_dropshadow).visibility = View.GONE
        snackBarView = view.findViewById(R.id.note_fragment_container)
        notesRecyclerView = view.findViewById<View>(R.id.notes_list) as RecyclerView
        emptyListText = view.findViewById<View>(R.id.empty_notes_list_tv) as TextView
        fab = view.findViewById<View>(R.id.add_note) as FloatingActionButton
        recyclerLayoutManager = LinearLayoutManager(activity)
        recyclerLayoutManager!!.reverseLayout = true
        recyclerLayoutManager!!.stackFromEnd = true
        notesRecyclerView!!.layoutManager = recyclerLayoutManager
        notesRecyclerView!!.adapter = instance!!.notesAdapter
        initListScrollListener()
        initListDataChangeListener()
        return view
    }

    private fun initListScrollListener() {
        notesRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && fab!!.isShown) {
                    // Hide the FAB when the user scrolls down.
                    fab!!.hide()
                }
                if (!recyclerView.canScrollVertically(-1)) {
                    // we have reached the top of the list
                    instance!!.mainActivityRef!!.findViewById<View>(R.id.toolbar_dropshadow).visibility =
                        View.GONE
                } else {
                    // we are not at the top yet
                    instance!!.mainActivityRef!!.findViewById<View>(R.id.toolbar_dropshadow).visibility =
                        View.VISIBLE
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Show the FAB when the user has stopped scrolling.
                    fab!!.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        fab!!.setOnClickListener { addNote() }
    }

    private fun initListDataChangeListener() {
        databaseManager!!.notesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                if (instance!!.notesAdapter!!.itemCount > 0) {
                    notesRecyclerView!!.visibility = View.VISIBLE
                    emptyListText!!.visibility = View.GONE

                    // Scroll to the "top" (bottom) to show changed Note.
                    notesRecyclerView!!.scrollToPosition(instance!!.notesAdapter!!.itemCount - 1)
                } else {
                    notesRecyclerView!!.visibility = View.GONE
                    emptyListText!!.visibility = View.VISIBLE
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val removedNote = dataSnapshot.getValue(
                    Note::class.java
                )
                if (removedNote != null) {
                    if (instance!!.archivedNotes.containsKey(removedNote.id)) {
                        // Note is Archived and not truly deleted.
                        Snackbar.make(
                            snackBarView!!,
                            "Note archived", Snackbar.LENGTH_LONG
                        )
                            .setAction("UNDO") { databaseManager!!.unarchiveNote(removedNote) }
                            .show()
                    } else {
                        Snackbar.make(
                            snackBarView!!,
                            "Note deleted", Snackbar.LENGTH_LONG
                        )
                            .setAction("UNDO") { databaseManager!!.addNote(removedNote) }.show()
                    }
                }
                if (instance!!.notesAdapter!!.itemCount > 0) {
                    notesRecyclerView!!.visibility = View.VISIBLE
                    emptyListText!!.visibility = View.GONE
                } else {
                    notesRecyclerView!!.visibility = View.GONE
                    emptyListText!!.visibility = View.VISIBLE
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    /**
     * Called when a user clicks on the Floating Action Button to add a new Note.
     */
    private fun addNote() {
        val addNoteIntent = Intent(instance, AddNoteActivity::class.java)
        startActivity(addNoteIntent)
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
                searchEditText.setTextColor(ContextCompat.getColor(instance!!, R.color.search_text_color))
                searchEditText.setHintTextColor(ContextCompat.getColor(instance!!, R.color.search_hint_color))
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
                return true
            }
            else -> {}
        }
        return false
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

    /**
     * Opens the Storage Access Framework file picker.
     */
    private fun openFilePicker() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        GoogleSignIn.getClient(instance!!, signInOptions)
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Opening file picker.")
            val pickerIntent =
                mDriveServiceHelper!!.createFilePickerIntent(DriveServiceHelper.TYPE_NOTE)

            // The result of the SAF Intent is handled in onActivityResult.
            startActivityForResult(pickerIntent, REQUEST_CODE_OPEN_FILE)
        }
    }

    /**
     * Opens a file from its `uri` returned from the Storage Access Framework file picker
     * initiated by [.openFilePicker].
     */
    private fun openFileFromFilePicker(uri: Uri) {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Opening " + uri.path)
            mDriveServiceHelper!!.openFileUsingStorageAccessFramework(instance!!.contentResolver, uri)
                .addOnSuccessListener { nameAndContent ->
                    val name = nameAndContent.first
                    val content = nameAndContent.second
                    Log.d(TAG, "Note picked: $name with content: $content")
                    try {
                        if (name != null && content != null) {
                            val jsonObject = JsonParser.parseString(content).asJsonObject
                            Log.d(TAG, "Parsed jsonObject: $jsonObject")
                            val importedNote = Gson().fromJson(jsonObject, Note::class.java)
                            Log.d(
                                TAG,
                                "Imported Note: " + importedNote.title + "(" + importedNote.id + ")"
                            )
                            val noteIdExists = (instance!!.notes.containsKey(importedNote.id)
                                    || instance!!.archivedNotes.containsKey(importedNote.id))
                            if (noteIdExists) {
                                // Change id of imported Note to avoid duplicates.
                                val newId = databaseManager!!.notesRef.push().key
                                importedNote.id = newId
                                Log.d(TAG, "New id: " + importedNote.id)
                            }
                            databaseManager!!.addNote(importedNote)
                            Toast.makeText(
                                instance,
                                "Note imported: " + importedNote.title,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(instance, "Note was empty!", Toast.LENGTH_LONG).show()
                        }
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
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        Log.d(TAG, "Query Submitted: $query")
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        Log.d(TAG, "Query Inserted: $newText")
        instance!!.notesAdapter!!.filter(newText)
        notesRecyclerView!!.scrollToPosition(instance!!.notesAdapter!!.itemCount - 1)
        return true
    }

    companion object {
        private val TAG = NotesFragment::class.java.simpleName
        private const val REQUEST_CODE_OPEN_FILE = 1
    }
}