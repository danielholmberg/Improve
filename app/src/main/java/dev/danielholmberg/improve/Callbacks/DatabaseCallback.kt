package dev.danielholmberg.improve.Callbacks

/**
 * Created by Daniel Holmberg.
 */
interface DatabaseCallback {
    fun onSuccess()
    fun onFailure(errorMessage: String?)
}