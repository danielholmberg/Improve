package dev.danielholmberg.improve.data.storage

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {

    private val uid: String
        get() = requireNotNull(auth.currentUser?.uid) { "User not authenticated" }

    private fun imageRef(noteId: String, imageId: String) =
        storage.reference.child("Users").child(uid).child("Images").child(noteId).child("$imageId.jpg")

    suspend fun uploadImage(
        noteId: String,
        imageId: String,
        localUri: String,
        onProgress: (Int) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(localUri)
            val ref = imageRef(noteId, imageId)
            suspendCancellableCoroutine { cont ->
                val uploadTask = ref.putFile(uri)
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    onProgress(progress)
                }
                uploadTask
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
                cont.invokeOnCancellation { uploadTask.cancel() }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteImage(noteId: String, imageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            suspendCancellableCoroutine { cont ->
                imageRef(noteId, imageId).delete()
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDownloadUrl(noteId: String, imageId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = suspendCancellableCoroutine { cont ->
                imageRef(noteId, imageId).downloadUrl
                    .addOnSuccessListener { cont.resume(it.toString()) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
