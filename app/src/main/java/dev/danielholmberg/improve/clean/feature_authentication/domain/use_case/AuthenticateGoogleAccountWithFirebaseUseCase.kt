package dev.danielholmberg.improve.clean.feature_authentication.domain.use_case

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dev.danielholmberg.improve.clean.feature_authentication.domain.repository.AuthRepository
import dev.danielholmberg.improve.clean.feature_authentication.util.AuthCallback

class AuthenticateGoogleAccountWithFirebaseUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(account: GoogleSignInAccount, callback: AuthCallback) {
        authRepository.authGoogleAccountWithFirebase(account, callback)
    }
}
