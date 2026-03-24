package dev.danielholmberg.improve.feature.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.usecase.notes.DeleteNoteUseCase
import dev.danielholmberg.improve.domain.usecase.notes.GetArchivedNotesUseCase
import dev.danielholmberg.improve.domain.usecase.notes.UnarchiveNoteUseCase
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

data class ArchivedNotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
)

sealed interface ArchivedNotesUiEvent {
    data class ShowSnackbar(val message: String) : ArchivedNotesUiEvent
}

@HiltViewModel
class ArchivedNotesViewModel @Inject constructor(
    getArchivedNotesUseCase: GetArchivedNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val unarchiveNoteUseCase: UnarchiveNoteUseCase,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _uiState = MutableStateFlow(ArchivedNotesUiState())
    val uiState: StateFlow<ArchivedNotesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ArchivedNotesUiEvent>()
    val events: SharedFlow<ArchivedNotesUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                getArchivedNotesUseCase(),
                _searchQuery,
            ) { allNotes, query ->
                if (query.isBlank()) {
                    allNotes
                } else {
                    allNotes.filter { note ->
                        note.title.contains(query, ignoreCase = true) ||
                            note.info.contains(query, ignoreCase = true)
                    }
                }
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
                    _events.emit(ArchivedNotesUiEvent.ShowSnackbar("Note deleted"))
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Failed to delete note"
                    _events.emit(ArchivedNotesUiEvent.ShowSnackbar(message))
                }
        }
    }

    fun unarchiveNote(note: Note) {
        viewModelScope.launch {
            unarchiveNoteUseCase(note)
                .onSuccess {
                    _events.emit(ArchivedNotesUiEvent.ShowSnackbar("Note restored"))
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Failed to restore note"
                    _events.emit(ArchivedNotesUiEvent.ShowSnackbar(message))
                }
        }
    }
}
