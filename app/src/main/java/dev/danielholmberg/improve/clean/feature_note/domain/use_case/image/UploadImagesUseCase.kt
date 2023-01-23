package dev.danielholmberg.improve.clean.feature_note.domain.use_case.image

import dev.danielholmberg.improve.clean.feature_note.domain.model.Image
import dev.danielholmberg.improve.clean.feature_note.domain.repository.ImageRepository
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback
import java.util.ArrayList

class UploadImagesUseCase(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(imageList: ArrayList<Image>, callback: ImageCallback) {
        imageRepository.uploadMultipleImages(imageList, callback)
    }
}
