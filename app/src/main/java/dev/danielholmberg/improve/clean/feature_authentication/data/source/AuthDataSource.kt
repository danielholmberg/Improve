package dev.danielholmberg.improve.clean.feature_authentication.data.source

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dev.danielholmberg.improve.clean.feature_authentication.util.AuthCallback

interface AuthDataSource {
    fun signInAnonymously(callback: AuthCallback)
    fun authGoogleAccountWithFirebase(
        account: GoogleSignInAccount,
        callback: AuthCallback
    )
    fun signOutGoogleAccount(callback: AuthCallback)
    fun signOutAnonymousAccount(callback: AuthCallback)
    fun linkAccount(account: GoogleSignInAccount, callback: AuthCallback)
    fun deleteAccountData(uid: String)
    fun getCurrentUserId(): String?
}