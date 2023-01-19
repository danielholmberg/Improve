package dev.danielholmberg.improve.clean.feature_note.data.source.note

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dev.danielholmberg.improve.clean.feature_authentication.data.source.AuthDataSourceImpl.Companion.USERS_REF
import dev.danielholmberg.improve.clean.feature_authentication.domain.repository.AuthRepository
import dev.danielholmberg.improve.clean.feature_note.data.source.entity.NoteEntity
import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository

class NoteDataSourceImpl(
    private val authRepository: AuthRepository,
    private val databaseService: FirebaseDatabase
) : NoteDataSource {
    private val userRef: DatabaseReference
        get() {
            val userId = authRepository.getCurrentUserId()
            return databaseService.getReference(USERS_REF).child(userId!!)
        }
    private val notesRef: DatabaseReference
        get() = userRef.child(NOTES_REF)
    private val archivedNotesRef: DatabaseReference
        get() = userRef.child(ARCHIVED_NOTES_REF)

    override fun addNote(noteEntity: NoteEntity) {
        notesRef.child(noteEntity.id!!).setValue(noteEntity) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to add Note (" + noteEntity.id + "): " + databaseError)
            }
        }
    }

    override fun updateNote(updatedNoteEntity: NoteEntity) {
        notesRef.child(updatedNoteEntity.id!!).setValue(updatedNoteEntity) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(
                    TAG,
                    "Failed to update Note (" + updatedNoteEntity.id + "): " + databaseError
                )
            }
        }
    }

    override fun deleteNote(noteEntityToDelete: NoteEntity, onSuccess: () -> Unit) {
        notesRef.child(noteEntityToDelete.id!!).removeValue { databaseError, _ ->
            if (databaseError != null) {
                Log.e(
                    TAG,
                    "Failed to delete Note (${noteEntityToDelete.id}): " + databaseError
                )
            }

            onSuccess()
        }
    }

    override fun saveNotes(noteEntities: Map<String?, NoteEntity>) {
        notesRef.updateChildren(noteEntities) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to save Notes: $databaseError")
            }
        }
    }

    override fun addArchivedNote(archivedNoteEntity: NoteEntity) {
        archivedNotesRef.child(archivedNoteEntity.id!!)
            .setValue(archivedNoteEntity) { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(
                        TAG,
                        "Failed to add Archived Note (" + archivedNoteEntity.id + "): " + databaseError
                    )
                }
            }
    }

    override fun updateArchivedNote(archivedNoteEntity: NoteEntity) {
        archivedNotesRef.child(archivedNoteEntity.id!!)
            .setValue(archivedNoteEntity) { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(
                        TAG,
                        "Failed to update Archived Note (" + archivedNoteEntity.id + "): " + databaseError
                    )
                }
            }
    }

    override fun deleteNoteFromArchive(noteEntityToDelete: NoteEntity, onSuccess: () -> Unit) {
        archivedNotesRef.child(noteEntityToDelete.id!!).removeValue { databaseError, _ ->
            if (databaseError != null) {
                Log.e(
                    TAG,
                    "Failed to delete Archived Note (" + noteEntityToDelete.id + "): " + databaseError
                )
            }
            onSuccess()
        }
    }

    override fun saveArchivedNotes(archivedNotes: Map<String?, NoteEntity>) {
        archivedNotesRef.updateChildren(archivedNotes) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to save Archived notes: $databaseError")
            }
        }

    }

    override fun addChildEventListener(childEventListener: ChildEventListener) {
        notesRef.addChildEventListener(childEventListener)
    }

    override fun addChildEventListenerForArchive(childEventListener: ChildEventListener) {
        archivedNotesRef.addChildEventListener(childEventListener)
    }

    override fun generateNewNoteId(): String? {
        return notesRef.push().key
    }

    companion object {
        private val TAG = NoteRepository::class.java.simpleName
        private const val NOTES_REF = "notes"
        private const val ARCHIVED_NOTES_REF = "archived_notes"
    }
}