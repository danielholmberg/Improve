package dev.danielholmberg.improve.feature.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.danielholmberg.improve.domain.model.Tag
import dev.danielholmberg.improve.domain.usecase.tags.AddTagUseCase
import dev.danielholmberg.improve.domain.usecase.tags.DeleteTagUseCase
import dev.danielholmberg.improve.domain.usecase.tags.GetTagsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagsUiState(
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = false,
)

sealed interface TagsUiEvent {
    data class ShowSnackbar(val message: String) : TagsUiEvent
}

@HiltViewModel
class TagsViewModel @Inject constructor(
    getTagsUseCase: GetTagsUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagsUiState())
    val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TagsUiEvent>()
    val events: SharedFlow<TagsUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getTagsUseCase().collect { tags ->
                _uiState.update { it.copy(tags = tags, isLoading = false) }
            }
        }
    }

    fun addTag(tag: Tag) {
        viewModelScope.launch {
            addTagUseCase(tag)
                .onSuccess {
                    _events.emit(TagsUiEvent.ShowSnackbar("Tag \"${tag.label}\" added"))
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Failed to add tag"
                    _events.emit(TagsUiEvent.ShowSnackbar(message))
                }
        }
    }

    fun deleteTag(tagId: String) {
        viewModelScope.launch {
            deleteTagUseCase(tagId)
                .onSuccess {
                    _events.emit(TagsUiEvent.ShowSnackbar("Tag deleted"))
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Failed to delete tag"
                    _events.emit(TagsUiEvent.ShowSnackbar(message))
                }
        }
    }
}
