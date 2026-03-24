package dev.danielholmberg.improve.data.firebase.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dev.danielholmberg.improve.data.firebase.dto.NoteDto
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseArchivedNoteDataSource @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) {

    private val uid: String
        get() = requireNotNull(auth.currentUser?.uid) { "User not authenticated" }

    private fun archivedNotesRef(): DatabaseReference =
        database.reference.child("users").child(uid).child("archived_notes")

    sealed class ArchivedNoteEvent {
        data class Added(val dto: NoteDto) : ArchivedNoteEvent()
        data class Changed(val dto: NoteDto) : ArchivedNoteEvent()
        data class Removed(val id: String) : ArchivedNoteEvent()
    }

    fun observeArchivedNotes(): Flow<ArchivedNoteEvent> = callbackFlow {
        val ref = archivedNotesRef()
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(NoteDto::class.java)?.let { trySend(ArchivedNoteEvent.Added(it)) }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(NoteDto::class.java)?.let { trySend(ArchivedNoteEvent.Changed(it)) }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                trySend(ArchivedNoteEvent.Removed(snapshot.key ?: return))
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onCancelled(error: DatabaseError) {
                cancel(error.message, error.toException())
            }
        }
        ref.addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun deleteArchivedNote(noteId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { cont ->
                archivedNotesRef().child(noteId).removeValue()
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
