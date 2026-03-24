package dev.danielholmberg.improve.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.danielholmberg.improve.domain.model.Feedback
import dev.danielholmberg.improve.domain.usecase.auth.GetCurrentUserUseCase
import dev.danielholmberg.improve.domain.usecase.feedback.SubmitFeedbackUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackUiState(
    val title: String = "",
    val feedback: String = "",
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val titleError: String? = null,
    val feedbackError: String? = null,
)

sealed interface FeedbackUiEvent {
    data class ShowSnackbar(val message: String) : FeedbackUiEvent
}

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FeedbackUiEvent>()
    val events: SharedFlow<FeedbackUiEvent> = _events.asSharedFlow()

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    fun onFeedbackChange(feedback: String) {
        _uiState.update { it.copy(feedback = feedback, feedbackError = null) }
    }

    fun submitFeedback() {
        val state = _uiState.value
        var hasError = false

        val titleError = if (state.title.isBlank()) {
            hasError = true
            "Subject is required"
        } else {
            null
        }

        val feedbackError = if (state.feedback.isBlank()) {
            hasError = true
            "Feedback is required"
        } else {
            null
        }

        if (hasError) {
            _uiState.update { it.copy(titleError = titleError, feedbackError = feedbackError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val uid = getCurrentUserUseCase().first()?.uid ?: ""
            val feedbackModel = Feedback(
                uid = uid,
                title = state.title.trim(),
                feedback = state.feedback.trim(),
            )

            submitFeedbackUseCase(feedbackModel)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSubmitted = true) }
                    _events.emit(FeedbackUiEvent.ShowSnackbar("Thank you for your feedback!"))
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(
                        FeedbackUiEvent.ShowSnackbar(
                            throwable.message ?: "Failed to submit feedback",
                        ),
                    )
                }
        }
    }
}
