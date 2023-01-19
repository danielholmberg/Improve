package dev.danielholmberg.improve.clean.feature_note.presentation.util

interface ImageCallback {
    fun onSuccess(`object`: Any)
    fun onFailure(errorMessage: String?)
    fun onProgress(progress: Int)
}