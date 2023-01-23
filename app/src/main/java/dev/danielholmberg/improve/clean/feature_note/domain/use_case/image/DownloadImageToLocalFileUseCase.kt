package dev.danielholmberg.improve.clean.feature_note.domain.use_case.image

import dev.danielholmberg.improve.clean.feature_note.domain.repository.ImageRepository
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback

class DownloadImageToLocalFileUseCase(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(imageId: String, imageCallback: ImageCallback) {
        imageRepository.downloadImageToLocalFile(imageId, imageCallback)
    }
}
