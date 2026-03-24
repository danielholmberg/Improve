package dev.danielholmberg.improve.domain.usecase.auth

import dev.danielholmberg.improve.domain.repository.AuthRepository
import javax.inject.Inject

class IsVipUserUseCase @Inject constructor(private val authRepository: AuthRepository) {
    operator fun invoke(email: String): Boolean = authRepository.isVipUser(email)
}
