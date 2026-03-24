package dev.danielholmberg.improve.feature.notes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.model.Tag
import dev.danielholmberg.improve.domain.usecase.auth.GetCurrentUserUseCase
import dev.danielholmberg.improve.domain.usecase.auth.IsVipUserUseCase
import dev.danielholmberg.improve.domain.usecase.notes.AddNoteUseCase
import dev.danielholmberg.improve.domain.usecase.notes.GetNotesUseCase
import dev.danielholmberg.improve.domain.usecase.notes.UpdateNoteUseCase
import dev.danielholmberg.improve.domain.usecase.tags.GetTagsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class AddEditNoteUiState(
    val noteId: String = "",
    val title: String = "",
    val info: String = "",
    val isStarred: Boolean = false,
    val selectedTagIds: Set<String> = emptySet(),
    val availableTags: List<Tag> = emptyList(),
    val vipImages: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isVip: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
)

sealed interface AddEditNoteUiEvent {
    data object NavigateBack : AddEditNoteUiEvent
    data class ShowSnackbar(val message: String) : AddEditNoteUiEvent
}

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val isVipUserUseCase: IsVipUserUseCase,
) : ViewModel() {

    private val noteId: String? = savedStateHandle["noteId"]
    private val isEditMode: Boolean get() = noteId != null

    private val _uiState = MutableStateFlow(AddEditNoteUiState())
    val uiState: StateFlow<AddEditNoteUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddEditNoteUiEvent>()
    val events: SharedFlow<AddEditNoteUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            // Observe tags
            launch {
                getTagsUseCase().collect { tags ->
                    _uiState.update { it.copy(availableTags = tags) }
                }
            }

            // Observe VIP status
            launch {
                getCurrentUserUseCase().collect { user ->
                    val isVip = user?.email?.let { email -> isVipUserUseCase(email) } ?: false
                    _uiState.update { it.copy(isVip = isVip) }
                }
            }

            // Load existing note if editing
            if (isEditMode && noteId != null) {
                _uiState.update { it.copy(isLoading = true) }
                val notes = getNotesUseCase().first()
                val note = notes.find { it.id == noteId }
                if (note != null) {
                    _uiState.update { state ->
                        state.copy(
                            noteId = note.id,
                            title = note.title,
                            info = note.info,
                            isStarred = note.isStarred,
                            selectedTagIds = note.tags.keys.toSet(),
                            vipImages = note.vipImages,
                            isLoading = false,
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Note not found") }
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, error = null) }
    }

    fun updateInfo(info: String) {
        _uiState.update { it.copy(info = info) }
    }

    fun toggleStar() {
        _uiState.update { it.copy(isStarred = !it.isStarred) }
    }

    fun toggleTag(tagId: String) {
        _uiState.update { state ->
            val current = state.selectedTagIds.toMutableSet()
            if (current.contains(tagId)) {
                current.remove(tagId)
            } else {
                current.add(tagId)
            }
            state.copy(selectedTagIds = current)
        }
    }

    fun addVipImageId(imageId: String) {
        _uiState.update { state ->
            state.copy(vipImages = state.vipImages + imageId)
        }
    }

    fun removeVipImage(imageId: String) {
        _uiState.update { state ->
            state.copy(vipImages = state.vipImages.filter { it != imageId })
        }
    }

    fun saveNote() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(error = "Title is required") }
            viewModelScope.launch {
                _events.emit(AddEditNoteUiEvent.ShowSnackbar("Title is required"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())

            val note = Note(
                id = if (isEditMode) state.noteId else UUID.randomUUID().toString(),
                title = state.title.trim(),
                info = state.info.trim(),
                isStarred = state.isStarred,
                tags = state.selectedTagIds.associateWith { true },
                vipImages = state.vipImages,
                added = if (isEditMode) state.noteId else timestamp,
                updated = timestamp,
            )

            val result = if (isEditMode) {
                updateNoteUseCase(note)
            } else {
                addNoteUseCase(note)
            }

            result
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                    _events.emit(AddEditNoteUiEvent.NavigateBack)
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Failed to save note"
                    _uiState.update { it.copy(isLoading = false, error = message) }
                    _events.emit(AddEditNoteUiEvent.ShowSnackbar(message))
                }
        }
    }
}
