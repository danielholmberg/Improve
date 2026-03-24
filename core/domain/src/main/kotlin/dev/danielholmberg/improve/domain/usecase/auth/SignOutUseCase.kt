package dev.danielholmberg.improve.domain.usecase.auth

import dev.danielholmberg.improve.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> =
        authRepository.signOut()
}
