package dev.danielholmberg.improve.clean.feature_authentication.domain.use_case

import dev.danielholmberg.improve.clean.feature_authentication.domain.repository.AuthRepository
import dev.danielholmberg.improve.clean.feature_authentication.util.AuthCallback

class SignInAnonymouslyUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(callback: AuthCallback) {
        authRepository.signInAnonymously(callback)
    }
}
