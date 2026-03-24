package dev.danielholmberg.improve.data.repository

import dev.danielholmberg.improve.data.firebase.datasource.FirebaseAuthDataSource
import dev.danielholmberg.improve.domain.model.UserProfile
import dev.danielholmberg.improve.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) : AuthRepository {

    override fun getCurrentUser(): Flow<UserProfile?> =
        authDataSource.authStateChanges().map { firebaseUser ->
            if (firebaseUser == null) return@map null
            val email = firebaseUser.email.orEmpty()
            UserProfile(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName.orEmpty(),
                email = email,
                photoUrl = firebaseUser.photoUrl?.toString().orEmpty(),
                isAnonymous = firebaseUser.isAnonymous,
                isVip = isVipUser(email)
            )
        }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> =
        authDataSource.signInWithGoogle(idToken)

    override suspend fun signInAnonymously(): Result<Unit> =
        authDataSource.signInAnonymously()

    override suspend fun signOut(): Result<Unit> =
        authDataSource.signOut()

    override fun isVipUser(email: String): Boolean =
        email.isNotEmpty() && email in VIP_USERS

    companion object {
        private val VIP_USERS = listOf(
            "daniel@holmberg.dev"
        )
    }
}
