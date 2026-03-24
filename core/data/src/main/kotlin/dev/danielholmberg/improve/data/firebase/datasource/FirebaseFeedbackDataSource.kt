package dev.danielholmberg.improve.data.firebase.datasource

import com.google.firebase.database.FirebaseDatabase
import dev.danielholmberg.improve.data.firebase.dto.FeedbackDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseFeedbackDataSource @Inject constructor(
    private val database: FirebaseDatabase
) {

    private fun feedbackRef() = database.reference.child("feedback")

    suspend fun submitFeedback(dto: FeedbackDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val id = dto.feedback_id ?: feedbackRef().push().key
                ?: return@withContext Result.failure(IllegalStateException("Failed to generate key"))
            val dtoWithId = dto.copy(feedback_id = id)
            suspendCancellableCoroutine { cont ->
                feedbackRef().child(id).setValue(dtoWithId)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
