package dev.danielholmberg.improve.legacy.Callbacks

/**
 * Created by Daniel Holmberg.
 */
interface AuthCallback {
    fun onSuccess()
    fun onFailure(errorMessage: String?)
}