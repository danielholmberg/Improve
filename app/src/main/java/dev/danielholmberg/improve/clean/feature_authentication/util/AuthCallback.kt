package dev.danielholmberg.improve.clean.feature_authentication.util

interface AuthCallback {
    fun onSuccess()
    fun onFailure(errorMessage: String?)
}