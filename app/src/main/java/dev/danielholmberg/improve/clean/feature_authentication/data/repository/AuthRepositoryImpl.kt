package dev.danielholmberg.improve.clean.feature_authentication.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dev.danielholmberg.improve.clean.feature_authentication.data.source.AuthDataSource
import dev.danielholmberg.improve.clean.feature_authentication.domain.repository.AuthRepository
import dev.danielholmberg.improve.clean.feature_authentication.util.AuthCallback

class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource
) : AuthRepository {
    override fun getCurrentUserId(): String? {
        return authDataSource.getCurrentUserId()
    }

    override fun signInAnonymously(callback: AuthCallback) {
        authDataSource.signInAnonymously(callback)
    }

    override fun authGoogleAccountWithFirebase(
        account: GoogleSignInAccount,
        callback: AuthCallback
    ) {
        authDataSource.authGoogleAccountWithFirebase(account, callback)
    }

    override fun signOutGoogleAccount(callback: AuthCallback) {
        authDataSource.signOutGoogleAccount(callback)
    }

    override fun signOutAnonymousAccount(callback: AuthCallback) {
        authDataSource.signOutAnonymousAccount(callback)
    }

    override fun linkAccount(account: GoogleSignInAccount, callback: AuthCallback) {
        authDataSource.linkAccount(account, callback)
    }

    override fun deleteAccountData(uid: String) {
        authDataSource.deleteAccountData(uid)
    }
}