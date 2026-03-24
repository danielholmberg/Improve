package dev.danielholmberg.improve.domain.usecase.auth

import dev.danielholmberg.improve.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<Unit> =
        authRepository.signInWithGoogle(idToken)
}
