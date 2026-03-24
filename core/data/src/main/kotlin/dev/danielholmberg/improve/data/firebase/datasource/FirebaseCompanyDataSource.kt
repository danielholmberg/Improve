package dev.danielholmberg.improve.data.firebase.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dev.danielholmberg.improve.data.firebase.dto.CompanyDto
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

class FirebaseCompanyDataSource @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) {

    private val uid: String
        get() = requireNotNull(auth.currentUser?.uid) { "User not authenticated" }

    private fun companiesRef(): DatabaseReference =
        database.reference.child("users").child(uid).child("companies")

    fun contactsRef(companyId: String): DatabaseReference =
        companiesRef().child(companyId).child("contacts")

    sealed class CompanyEvent {
        data class Added(val dto: CompanyDto) : CompanyEvent()
        data class Changed(val dto: CompanyDto) : CompanyEvent()
        data class Removed(val id: String) : CompanyEvent()
    }

    fun observeCompanies(): Flow<CompanyEvent> = callbackFlow {
        val ref = companiesRef()
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val dto = CompanyDto(
                    id = snapshot.key,
                    name = snapshot.child("name").getValue(String::class.java)
                )
                trySend(CompanyEvent.Added(dto))
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val dto = CompanyDto(
                    id = snapshot.key,
                    name = snapshot.child("name").getValue(String::class.java)
                )
                trySend(CompanyEvent.Changed(dto))
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                trySend(CompanyEvent.Removed(snapshot.key ?: return))
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onCancelled(error: DatabaseError) {
                cancel(error.message, error.toException())
            }
        }
        ref.addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addCompany(dto: CompanyDto): Result<String> = withContext(Dispatchers.IO) {
        try {
            val id = dto.id ?: companiesRef().push().key
                ?: return@withContext Result.failure(IllegalStateException("Failed to generate key"))
            val dtoWithId = dto.copy(id = id)
            suspendCancellableCoroutine { cont ->
                companiesRef().child(id).child("id").setValue(dtoWithId.id)
                companiesRef().child(id).child("name").setValue(dtoWithId.name)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCompany(companyId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { cont ->
                companiesRef().child(companyId).removeValue()
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeContactIds(companyId: String): Flow<Map<String, Boolean>> = callbackFlow {
        val ref = contactsRef(companyId)
        val listener = object : ChildEventListener {
            private val contactMap = mutableMapOf<String, Boolean>()

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val key = snapshot.key ?: return
                contactMap[key] = snapshot.getValue(Boolean::class.java) ?: true
                trySend(contactMap.toMap())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val key = snapshot.key ?: return
                contactMap[key] = snapshot.getValue(Boolean::class.java) ?: true
                trySend(contactMap.toMap())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val key = snapshot.key ?: return
                contactMap.remove(key)
                trySend(contactMap.toMap())
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onCancelled(error: DatabaseError) {
                cancel(error.message, error.toException())
            }
        }
        ref.addChildEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
