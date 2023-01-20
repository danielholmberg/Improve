package dev.danielholmberg.improve.clean.feature_authentication.domain.use_case

import dev.danielholmberg.improve.clean.feature_authentication.domain.repository.AuthRepository

class CheckIfAlreadyAuthenticatedUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return authRepository.getCurrentUserId() != null
    }
}