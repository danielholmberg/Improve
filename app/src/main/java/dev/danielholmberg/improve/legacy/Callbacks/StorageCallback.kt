package dev.danielholmberg.improve.legacy.Callbacks

interface StorageCallback {
    fun onSuccess(`object`: Any)
    fun onFailure(errorMessage: String?)
    fun onProgress(progress: Int)
}