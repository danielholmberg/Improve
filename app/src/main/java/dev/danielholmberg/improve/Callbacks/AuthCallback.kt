package dev.danielholmberg.improve.Callbacks

/**
 * Created by Daniel Holmberg.
 */
interface AuthCallback {
    fun onSuccess()
    fun onFailure(errorMessage: String?)
}