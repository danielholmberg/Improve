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

class FirebaseNoteDataSource @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) {

    private val uid: String
        get() = requireNotNull(auth.currentUser?.uid) { "User not authenticated" }

    private fun notesRef(): DatabaseReference =
        database.reference.child("users").child(uid).child("notes")

    private fun archivedNotesRef(): DatabaseReference =
        database.reference.child("users").child(uid).child("archived_notes")

    sealed class NoteEvent {
        data class Added(val dto: NoteDto) : NoteEvent()
        data class Changed(val dto: NoteDto) : NoteEvent()
        data class Removed(val id: String) : NoteEvent()
    }

    fun observeNotes(): Flow<NoteEvent> = callbackFlow {
        val ref = notesRef()
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(NoteDto::class.java)?.let { trySend(NoteEvent.Added(it)) }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(NoteDto::class.java)?.let { trySend(NoteEvent.Changed(it)) }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                trySend(NoteEvent.Removed(snapshot.key ?: return))
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onCancelled(error: DatabaseError) {
                cancel(error.message, error.toException())
            }
        }
        ref.addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addNote(dto: NoteDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val id = dto.id ?: notesRef().push().key
                ?: return@withContext Result.failure(IllegalStateException("Failed to generate key"))
            val dtoWithId = dto.copy(id = id)
            suspendCancellableCoroutine { cont ->
                notesRef().child(id).setValue(dtoWithId)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(dto: NoteDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val id = dto.id ?: return@withContext Result.failure(IllegalArgumentException("Note id is null"))
            suspendCancellableCoroutine { cont ->
                notesRef().child(id).setValue(dto)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String, vipImages: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { cont ->
                notesRef().child(noteId).removeValue()
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun archiveNote(dto: NoteDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val id = dto.id ?: return@withContext Result.failure(IllegalArgumentException("Note id is null"))
            val archivedDto = dto.copy(archived = true)
            // Add to archived_notes
            suspendCancellableCoroutine { cont ->
                archivedNotesRef().child(id).setValue(archivedDto)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            // Remove from notes
            suspendCancellableCoroutine { cont ->
                notesRef().child(id).removeValue()
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unarchiveNote(dto: NoteDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val id = dto.id ?: return@withContext Result.failure(IllegalArgumentException("Note id is null"))
            val activeDto = dto.copy(archived = false)
            // Add back to notes
            suspendCancellableCoroutine { cont ->
                notesRef().child(id).setValue(activeDto)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            // Remove from archived_notes
            suspendCancellableCoroutine { cont ->
                archivedNotesRef().child(id).removeValue()
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun starNote(noteId: String, starred: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { cont ->
                notesRef().child(noteId).child("stared").setValue(starred)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
