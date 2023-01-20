package dev.danielholmberg.improve.clean.feature_authentication.presentation

import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dev.danielholmberg.improve.clean.feature_authentication.domain.use_case.AuthUseCases
import dev.danielholmberg.improve.clean.feature_authentication.util.AuthCallback

class SignInViewModel(
    private val authUseCases: AuthUseCases
) : ViewModel() {
    fun isAlreadySignedIn(): Boolean {
        return authUseCases.checkIfAlreadyAuthenticatedUseCase()
    }

    fun authenticateGoogleAccountWithFirebase(account: GoogleSignInAccount, authCallback: AuthCallback) {
        authUseCases.authenticateGoogleAccountWithFirebaseUseCase(account, authCallback)
    }

    fun signInAnonymously(authCallback: AuthCallback) {
        authUseCases.signInAnonymouslyUseCase(authCallback)
    }

}