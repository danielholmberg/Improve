package dev.danielholmberg.improve.feature.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.usecase.notes.ArchiveNoteUseCase
import dev.danielholmberg.improve.domain.usecase.notes.DeleteNoteUseCase
import dev.danielholmberg.improve.domain.usecase.notes.ExportNotesUseCase
import dev.danielholmberg.improve.domain.usecase.notes.GetNotesUseCase
import dev.danielholmberg.improve.domain.usecase.notes.StarNoteUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
)

sealed interface NotesUiEvent {
    data class ShowSnackbar(val message: String) : NotesUiEvent
    data object NavigateToAddNote : NotesUiEvent
    data class NavigateToNoteDetail(val noteId: String) : NotesUiEvent
}

@HiltViewModel
class NotesViewModel @Inject constructor(
    getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val archiveNoteUseCase: ArchiveNoteUseCase,
    private val starNoteUseCase: StarNoteUseCase,
    private val exportNotesUseCase: ExportNotesUseCase,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<NotesUiEvent>()
    val events: SharedFlow<NotesUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                getNotesUseCase(),
                _searchQuery,
            ) { allNotes, query ->
                val filtered = if (query.isBlank()) {
                    allNotes
                } else {
                    allNotes.filter { note ->
                        note.title.contains(query, ignoreCase = true) ||
                            note.info.contains(query, ignoreCase = true)
                    }
                }
                filtered
            }.collect { filteredNotes ->
                _uiState.update { state ->
                    state.copy(
                        notes = filteredNotes,
                        isLoading = false,
                        searchQuery = _searchQuery.value,
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            deleteNoteUseCase(note)
                .onSuccess {
                    _events.emit(NotesUiEvent.ShowSnackbar("Note deleted"))
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Failed to delete note"
                    _uiState.update { it.copy(error = message) }
                    _events.emit(NotesUiEvent.ShowSnackbar(message))
                }
        }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch {
            archiveNoteUseCase(note)
                .onSuccess {
                    _events.emit(NotesUiEvent.ShowSnackbar("Note archived"))
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Failed to archive note"
                    _uiState.update { it.copy(error = message) }
                    _events.emit(NotesUiEvent.ShowSnackbar(message))
                }
        }
    }

    fun toggleStar(note: Note) {
        viewModelScope.launch {
            val newStarred = !note.isStarred
            starNoteUseCase(note.id, newStarred)
                .onFailure { throwable ->
                    val message = throwable.message ?: "Failed to update note"
                    _events.emit(NotesUiEvent.ShowSnackbar(message))
                }
        }
    }

    fun exportNotes() {
        viewModelScope.launch {
            val notes = _uiState.value.notes
            if (notes.isEmpty()) {
                _events.emit(NotesUiEvent.ShowSnackbar("No notes to export"))
                return@launch
            }
            _uiState.update { it.copy(isLoading = true) }
            exportNotesUseCase(notes, emptyMap())
                .onSuccess { message ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(NotesUiEvent.ShowSnackbar(message))
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Export failed"
                    _uiState.update { it.copy(isLoading = false, error = message) }
                    _events.emit(NotesUiEvent.ShowSnackbar(message))
                }
        }
    }
}
