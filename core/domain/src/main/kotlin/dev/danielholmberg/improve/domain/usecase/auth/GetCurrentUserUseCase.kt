package dev.danielholmberg.improve.domain.usecase.auth

import dev.danielholmberg.improve.domain.model.UserProfile
import dev.danielholmberg.improve.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<UserProfile?> = authRepository.getCurrentUser()
}
