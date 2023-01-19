package dev.danielholmberg.improve.clean.feature_note.data.repository

import android.net.Uri
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback
import dev.danielholmberg.improve.clean.feature_note.data.source.image.ImageDataSource
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image
import dev.danielholmberg.improve.clean.feature_note.domain.repository.ImageRepository

class ImageRepositoryImpl(
    private val imageDataSource: ImageDataSource
) : ImageRepository {
    override fun deleteImage(noteId: String, imageId: String) {
        imageDataSource.deleteImage(noteId, imageId)
    }

    override fun uploadImage(imageId: String?, imageUri: Uri?, callback: ImageCallback) {
        imageDataSource.uploadImage(imageId, imageUri, callback)
    }

    override fun downloadImageToLocalFile(imageId: String, callback: ImageCallback) {
        imageDataSource.downloadImageToLocalFile(imageId, callback)
    }

    override fun uploadMultipleImages(imagesList: List<Image>, callback: ImageCallback) {
        imageDataSource.uploadMultipleImages(imagesList, callback)
    }
}