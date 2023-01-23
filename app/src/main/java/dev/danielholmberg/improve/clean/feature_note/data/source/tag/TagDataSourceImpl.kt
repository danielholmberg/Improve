package dev.danielholmberg.improve.clean.feature_note.data.source.tag

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.feature_authentication.domain.repository.AuthRepository
import dev.danielholmberg.improve.clean.feature_note.data.source.entity.TagEntity
import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository

class TagDataSourceImpl(
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository,
    private val databaseService: FirebaseDatabase
) : TagDataSource {
    private val tagRef: DatabaseReference
        get() = databaseService.getReference(USERS_REF).child(
            authRepository.getCurrentUserId()!!
        ).child(TAGS_REF)

    override fun addTag(tagEntity: TagEntity) {
        tagRef.child(tagEntity.id!!).setValue(tagEntity) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to add Tag (" + tagEntity.id + ") to Firebase: " + databaseError)
                tagRef.child(tagEntity.id).removeValue()
            }
        }
    }

    override fun deleteTag(tagEntityId: String?) {
        tagRef.child(tagEntityId!!).removeValue { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to delete Tag: " + tagEntityId + "from Firebase: " + databaseError)
            } else {
                for (note in instance!!.notesAdapter!!.hashMap.values) {
                    note.removeTag(tagEntityId)
                    noteRepository.updateNote(note)
                }
                for (archivedNote in instance!!.archivedNotesAdapter!!.hashMap.values) {
                    archivedNote.removeTag(tagEntityId)
                    noteRepository.updateArchivedNote(archivedNote)
                }
            }
        }
    }

    override fun saveTags(tagEntities: Map<String?, TagEntity>) {
        tagRef.updateChildren(tagEntities) { databaseError, _ ->
            if (databaseError != null) {
                Log.e(TAG, "Failed to save Tags: $databaseError")
            }
        }
    }

    override fun addChildEventListener(childEventListener: ChildEventListener) {
        tagRef.addChildEventListener(childEventListener)
    }

    override fun generateNewTagId(): String? {
        return tagRef.push().key
    }

    companion object {
        private val TAG = TagDataSourceImpl::class.java.simpleName
        private const val TAGS_REF = "tags"
        private const val USERS_REF = "users"
    }
}