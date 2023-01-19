package dev.danielholmberg.improve.clean.feature_note.data.source.image

import android.net.Uri
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image

interface ImageDataSource {
    fun deleteImage(noteEntityId: String, imageEntityId: String)
    fun uploadImage(noteEntityId: String?, imageEntityUri: Uri?, callback: ImageCallback)
    fun downloadImageToLocalFile(imageEntityId: String, callback: ImageCallback)
    fun uploadMultipleImages(imageEntitiesList: List<Image>, callback: ImageCallback)
}