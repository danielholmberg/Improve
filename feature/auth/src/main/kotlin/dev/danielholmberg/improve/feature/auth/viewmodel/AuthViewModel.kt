package dev.danielholmberg.improve.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.danielholmberg.improve.domain.usecase.auth.GetCurrentUserUseCase
import dev.danielholmberg.improve.domain.usecase.auth.SignInAnonymouslyUseCase
import dev.danielholmberg.improve.domain.usecase.auth.SignInWithGoogleUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignedIn: Boolean = false,
)

sealed interface AuthUiEvent {
    data class ShowError(val message: String) : AuthUiEvent
    data object NavigateToMain : AuthUiEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthUiEvent>()
    val events: SharedFlow<AuthUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                if (user != null) {
                    _uiState.update { it.copy(isSignedIn = true) }
                    _events.emit(AuthUiEvent.NavigateToMain)
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            signInWithGoogleUseCase(idToken)
                .onSuccess {
                    // Navigation is handled via getCurrentUserUseCase flow observer in init
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Google sign-in failed"
                    _uiState.update { it.copy(isLoading = false, error = message) }
                    _events.emit(AuthUiEvent.ShowError(message))
                }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            signInAnonymouslyUseCase()
                .onSuccess {
                    // Navigation is handled via getCurrentUserUseCase flow observer in init
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Anonymous sign-in failed"
                    _uiState.update { it.copy(isLoading = false, error = message) }
                    _events.emit(AuthUiEvent.ShowError(message))
                }
        }
    }
}
