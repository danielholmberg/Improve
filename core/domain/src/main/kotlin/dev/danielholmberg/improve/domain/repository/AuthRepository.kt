package dev.danielholmberg.improve.domain.repository

import dev.danielholmberg.improve.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<UserProfile?>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun signInAnonymously(): Result<Unit>
    suspend fun signOut(): Result<Unit>
    fun isVipUser(email: String): Boolean
}
