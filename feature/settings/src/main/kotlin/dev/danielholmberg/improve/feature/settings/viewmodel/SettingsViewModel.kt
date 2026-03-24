package dev.danielholmberg.improve.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.danielholmberg.improve.domain.model.UserProfile
import dev.danielholmberg.improve.domain.usecase.auth.GetCurrentUserUseCase
import dev.danielholmberg.improve.domain.usecase.auth.SignOutUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
)

sealed interface SettingsUiEvent {
    data object NavigateToSignIn : SettingsUiEvent
    data class ShowSnackbar(val message: String) : SettingsUiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsUiEvent>()
    val events: SharedFlow<SettingsUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            signOutUseCase()
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(SettingsUiEvent.NavigateToSignIn)
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(
                        SettingsUiEvent.ShowSnackbar(
                            throwable.message ?: "Sign out failed",
                        ),
                    )
                }
        }
    }
}
