package dev.danielholmberg.improve.clean.feature_note.domain.repository

import android.net.Uri
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image

interface ImageRepository {
    fun deleteImage(noteId: String, imageId: String)
    fun uploadImage(imageId: String?, imageUri: Uri?, callback: ImageCallback)
    fun downloadImageToLocalFile(imageId: String, callback: ImageCallback)
    fun uploadMultipleImages(imagesList: List<Image>, callback: ImageCallback)
}