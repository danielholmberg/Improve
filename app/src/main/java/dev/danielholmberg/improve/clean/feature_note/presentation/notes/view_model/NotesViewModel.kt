package dev.danielholmberg.improve.clean.feature_note.presentation.notes.view_model

import android.util.Log
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ChildEventListener
import com.google.gson.Gson
import com.google.gson.JsonParser
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.core.FileService
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.ImageUseCases
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.NoteUseCases
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback
import java.io.File

class NotesViewModel(
    private val noteUseCases: NoteUseCases,
    private val imageUseCases: ImageUseCases
) {
    fun handleOnNoteAdded(note: Note) {
        instance!!.notesAdapter?.add(note)
        noteUseCases.addNoteUseCase(note)
        if (note.hasImage()) {
            downloadImages(note.images)
        }
    }

    fun handleOnNoteRemoved(removedNote: Note, snackBarView: CoordinatorLayout) {
        instance!!.notesAdapter?.remove(removedNote)

        if (instance!!.archivedNotes.containsKey(removedNote.id)) {
            // Note is Archived and not truly deleted.
            Snackbar.make(
                snackBarView,
                "Note archived", Snackbar.LENGTH_LONG
            )
                .setAction("UNDO") {
                    noteUseCases.unarchiveNoteUseCase(removedNote)
                }
                .show()
        } else {
            Snackbar.make(
                snackBarView,
                "Note deleted", Snackbar.LENGTH_LONG
            )
                .setAction("UNDO") {
                    noteUseCases.addNoteUseCase(removedNote)
                }.show()
        }
    }

    fun importNoteUseCase(name: String?, content: String?) {
        if (name == null || content == null) {
            Toast.makeText(instance, "Note was empty!", Toast.LENGTH_LONG).show()
            return
        }

        val jsonObject = JsonParser.parseString(content).asJsonObject
        Log.d(TAG, "Parsed jsonObject: $jsonObject")
        val importedNote = Gson().fromJson(jsonObject, Note::class.java)
        Log.d(TAG, "Imported Note (${importedNote.id}): ${importedNote.title}")
        val noteIdExists = (instance!!.notes.containsKey(importedNote.id)
                || instance!!.archivedNotes.containsKey(importedNote.id))
        if (noteIdExists) {
            // Change id of imported Note to avoid duplicates.
            val newId = noteUseCases.generateNewNoteUseCase().id
            importedNote.id = newId
        }

        noteUseCases.addNoteUseCase(importedNote)

        Toast.makeText(
            instance,
            "Note imported: ${importedNote.title}",
            Toast.LENGTH_LONG
        ).show()
    }

    fun addChildEventListener(childEventListener: ChildEventListener) {
        noteUseCases.addChildEventListenerUseCase(childEventListener)
    }

    private fun downloadImages(images: ArrayList<String>) {
        for (imageId in images) {
            val cachedImage = File(
                instance!!.fileService.imageDir,
                imageId + FileService.IMAGE_SUFFIX
            )
            if (cachedImage.exists()) {
                Log.d(TAG, "Image exists in Local Filesystem with image id: $imageId")
            } else {
                Log.d(TAG, "Downloading image from Firebase with image id: $imageId")
            }

            imageUseCases.downloadImageToLocalFileUseCase(imageId, object : ImageCallback {
                override fun onSuccess(`object`: Any) {}
                override fun onFailure(errorMessage: String?) {}
                override fun onProgress(progress: Int) {}
            })
        }
    }

    fun handleOnNoteChanged(updatedNote: Note) {
        if (checkIsExistingNote(updatedNote)) {
            updateNote(updatedNote)
        } else {
            addNote(updatedNote)
        }
    }

    private fun checkIsExistingNote(note: Note): Boolean {
        return instance!!.notesAdapter!!.contains(note)
    }

    private fun updateNote(updatedNote: Note) {
        instance!!.notesAdapter!!.update(updatedNote)
    }

    private fun addNote(note: Note) {
        instance!!.notesAdapter!!.add(note)
    }

    companion object {
        private val TAG = NotesViewModel::class.java.simpleName
    }
}