package dev.danielholmberg.improve.clean.feature_authentication.domain.use_case

data class AuthUseCases(
    val checkIfAlreadyAuthenticatedUseCase: CheckIfAlreadyAuthenticatedUseCase,
    val authenticateGoogleAccountWithFirebaseUseCase: AuthenticateGoogleAccountWithFirebaseUseCase,
    val signInAnonymouslyUseCase: SignInAnonymouslyUseCase
)
