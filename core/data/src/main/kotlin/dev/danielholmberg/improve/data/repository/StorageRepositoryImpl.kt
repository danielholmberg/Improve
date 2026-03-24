package dev.danielholmberg.improve.data.repository

import dev.danielholmberg.improve.data.storage.FirebaseStorageDataSource
import dev.danielholmberg.improve.domain.repository.StorageRepository
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val storageDataSource: FirebaseStorageDataSource
) : StorageRepository {

    override suspend fun uploadImage(
        noteId: String,
        imageId: String,
        localUri: String,
        onProgress: (Int) -> Unit
    ): Result<Unit> = storageDataSource.uploadImage(noteId, imageId, localUri, onProgress)

    override suspend fun deleteImage(noteId: String, imageId: String): Result<Unit> =
        storageDataSource.deleteImage(noteId, imageId)

    override suspend fun getImageDownloadUrl(noteId: String, imageId: String): Result<String> =
        storageDataSource.getDownloadUrl(noteId, imageId)
}
