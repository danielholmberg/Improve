package dev.danielholmberg.improve.domain.repository

interface StorageRepository {
    suspend fun uploadImage(
        noteId: String,
        imageId: String,
        localUri: String,
        onProgress: (Int) -> Unit
    ): Result<Unit>

    suspend fun deleteImage(noteId: String, imageId: String): Result<Unit>
    suspend fun getImageDownloadUrl(noteId: String, imageId: String): Result<String>
}
