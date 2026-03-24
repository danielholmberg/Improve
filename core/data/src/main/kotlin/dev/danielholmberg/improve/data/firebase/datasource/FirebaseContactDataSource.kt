package dev.danielholmberg.improve.data.firebase.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import dev.danielholmberg.improve.data.firebase.dto.ContactDto
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

class FirebaseContactDataSource @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val companyDataSource: FirebaseCompanyDataSource
) {

    private val uid: String
        get() = requireNotNull(auth.currentUser?.uid) { "User not authenticated" }

    private fun contactsRef(companyId: String) =
        database.reference.child("users").child(uid).child("companies").child(companyId).child("contacts")

    sealed class ContactEvent {
        data class Added(val dto: ContactDto, val companyId: String) : ContactEvent()
        data class Changed(val dto: ContactDto, val companyId: String) : ContactEvent()
        data class Removed(val id: String, val companyId: String) : ContactEvent()
    }

    fun observeContacts(companyId: String): Flow<ContactEvent> = callbackFlow {
        val ref = contactsRef(companyId)
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(ContactDto::class.java)?.let {
                    trySend(ContactEvent.Added(it, companyId))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(ContactDto::class.java)?.let {
                    trySend(ContactEvent.Changed(it, companyId))
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                trySend(ContactEvent.Removed(snapshot.key ?: return, companyId))
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onCancelled(error: DatabaseError) {
                cancel(error.message, error.toException())
            }
        }
        ref.addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addContact(dto: ContactDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val companyId = dto.companyId
                ?: return@withContext Result.failure(IllegalArgumentException("companyId is null"))
            val ref = contactsRef(companyId)
            val id = dto.id ?: ref.push().key
                ?: return@withContext Result.failure(IllegalStateException("Failed to generate key"))
            val dtoWithId = dto.copy(id = id)
            suspendCancellableCoroutine { cont ->
                ref.child(id).setValue(dtoWithId)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            // Register contact id under company contacts map
            suspendCancellableCoroutine { cont ->
                companyDataSource.contactsRef(companyId).child(id).setValue(true)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateContact(dto: ContactDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val companyId = dto.companyId
                ?: return@withContext Result.failure(IllegalArgumentException("companyId is null"))
            val id = dto.id
                ?: return@withContext Result.failure(IllegalArgumentException("Contact id is null"))
            suspendCancellableCoroutine { cont ->
                contactsRef(companyId).child(id).setValue(dto)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteContact(contactId: String, companyId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { cont ->
                contactsRef(companyId).child(contactId).removeValue()
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            // Remove from company contacts map
            suspendCancellableCoroutine { cont ->
                companyDataSource.contactsRef(companyId).child(contactId).removeValue()
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
