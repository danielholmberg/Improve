package dev.danielholmberg.improve.clean.feature_note.presentation.notes.view_model

import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ChildEventListener
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.NoteUseCases

class ArchivedNotesViewModel(
    private val noteUseCase: NoteUseCases
) : ViewModel() {
    fun addChildEventListenerForArchive(childEventListener: ChildEventListener) {
        noteUseCase.addChildEventListenerForArchiveUseCase(childEventListener)
    }

    fun handleOnArchiveNoteAdded(note: Note) {
        instance!!.archivedNotesAdapter!!.add(note)
        noteUseCase.addArchivedNoteUseCase(note)
    }

    fun handleOnArchivedNoteRemoved(removedArchivedNote: Note, snackBarView: CoordinatorLayout) {
        instance!!.archivedNotesAdapter?.remove(removedArchivedNote)

        if (instance!!.notes.containsKey(removedArchivedNote.id)) {
            // Note is Unarchived and not truly deleted.
            Snackbar.make(
                snackBarView,
                "Note unarchived", Snackbar.LENGTH_LONG
            )
                .setAction("UNDO") {
                    noteUseCase.archiveNoteUseCase(removedArchivedNote)
                }
                .show()
        } else {
            Snackbar.make(
                snackBarView,
                "Note deleted", Snackbar.LENGTH_LONG
            )
                .setAction("UNDO") {
                    noteUseCase.addArchivedNoteUseCase(removedArchivedNote)
                }.show()
        }
    }

    fun handleOnArchivedNoteUpdated(updatedNote: Note) {
        if (checkIsExistingArchivedNote(updatedNote)) {
            updateArchivedNote(updatedNote)
        } else {
            addArchivedNote(updatedNote)
        }
    }

    private fun checkIsExistingArchivedNote(note: Note): Boolean {
        return instance!!.archivedNotesAdapter!!.contains(note)
    }

    private fun updateArchivedNote(updatedNote: Note) {
        instance!!.archivedNotesAdapter!!.update(updatedNote)
    }

    private fun addArchivedNote(note: Note) {
        instance!!.archivedNotesAdapter!!.add(note)
    }


}