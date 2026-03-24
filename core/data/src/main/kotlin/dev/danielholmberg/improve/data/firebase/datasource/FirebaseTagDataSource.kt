package dev.danielholmberg.improve.data.firebase.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dev.danielholmberg.improve.data.firebase.dto.TagDto
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

class FirebaseTagDataSource @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) {

    private val uid: String
        get() = requireNotNull(auth.currentUser?.uid) { "User not authenticated" }

    private fun tagsRef(): DatabaseReference =
        database.reference.child("users").child(uid).child("tags")

    sealed class TagEvent {
        data class Added(val dto: TagDto) : TagEvent()
        data class Changed(val dto: TagDto) : TagEvent()
        data class Removed(val id: String) : TagEvent()
    }

    fun observeTags(): Flow<TagEvent> = callbackFlow {
        val ref = tagsRef()
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(TagDto::class.java)?.let { trySend(TagEvent.Added(it)) }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(TagDto::class.java)?.let { trySend(TagEvent.Changed(it)) }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                trySend(TagEvent.Removed(snapshot.key ?: return))
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onCancelled(error: DatabaseError) {
                cancel(error.message, error.toException())
            }
        }
        ref.addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addTag(dto: TagDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val id = dto.id ?: tagsRef().push().key
                ?: return@withContext Result.failure(IllegalStateException("Failed to generate key"))
            val dtoWithId = dto.copy(id = id)
            suspendCancellableCoroutine { cont ->
                tagsRef().child(id).setValue(dtoWithId)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTag(tagId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { cont ->
                tagsRef().child(tagId).removeValue()
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
