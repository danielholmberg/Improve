package dev.danielholmberg.improve.clean.feature_note.domain.use_case

import dev.danielholmberg.improve.clean.feature_note.domain.use_case.image.DownloadImageToLocalFileUseCase
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.image.UploadImagesUseCase

data class ImageUseCases(
    val uploadImagesUseCase: UploadImagesUseCase,
    val downloadImageToLocalFileUseCase: DownloadImageToLocalFileUseCase
)