package dev.danielholmberg.improve.clean.feature_authentication.domain.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dev.danielholmberg.improve.clean.feature_authentication.util.AuthCallback

interface AuthRepository {
    fun getCurrentUserId(): String?
    fun signInAnonymously(callback: AuthCallback)
    fun authGoogleAccountWithFirebase(
        account: GoogleSignInAccount,
        callback: AuthCallback
    )
    fun signOutGoogleAccount(callback: AuthCallback)
    fun signOutAnonymousAccount(callback: AuthCallback)
    fun linkAccount(account: GoogleSignInAccount, callback: AuthCallback)
    fun deleteAccountData(uid: String)
}