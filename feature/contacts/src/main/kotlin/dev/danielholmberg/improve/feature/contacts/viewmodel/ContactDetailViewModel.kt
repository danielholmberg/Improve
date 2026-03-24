package dev.danielholmberg.improve.feature.contacts.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.danielholmberg.improve.domain.model.Contact
import dev.danielholmberg.improve.domain.usecase.contacts.ExportContactUseCase
import dev.danielholmberg.improve.domain.usecase.contacts.GetCompaniesUseCase
import dev.danielholmberg.improve.domain.usecase.contacts.GetContactsUseCase
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

data class ContactDetailUiState(
    val contact: Contact? = null,
    val companyName: String? = null,
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
)

sealed interface ContactDetailUiEvent {
    data class ShowSnackbar(val message: String) : ContactDetailUiEvent
}

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getContactsUseCase: GetContactsUseCase,
    getCompaniesUseCase: GetCompaniesUseCase,
    private val exportContactUseCase: ExportContactUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactDetailUiState(isLoading = true))
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ContactDetailUiEvent>()
    val events: SharedFlow<ContactDetailUiEvent> = _events.asSharedFlow()

    init {
        val contactId: String = savedStateHandle["contactId"] ?: ""
        viewModelScope.launch {
            combine(
                getContactsUseCase(),
                getCompaniesUseCase(),
            ) { contacts, companies ->
                val contact = contacts.find { it.id == contactId }
                val companyName = companies.find { it.id == contact?.companyId }?.name
                contact to companyName
            }.collect { (contact, companyName) ->
                _uiState.update {
                    it.copy(
                        contact = contact,
                        companyName = companyName,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun exportToDrive() {
        val contact = _uiState.value.contact ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            // Build a minimal Company stub for the use case (only name is written to the export)
            val companyName = _uiState.value.companyName
            val company = if (companyName != null) {
                dev.danielholmberg.improve.domain.model.Company(
                    id = contact.companyId,
                    name = companyName,
                )
            } else {
                null
            }
            exportContactUseCase(contact, company)
                .onSuccess { fileId ->
                    _uiState.update { it.copy(isExporting = false) }
                    _events.emit(ContactDetailUiEvent.ShowSnackbar("Exported to Drive"))
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isExporting = false) }
                    _events.emit(
                        ContactDetailUiEvent.ShowSnackbar(
                            throwable.message ?: "Export failed",
                        ),
                    )
                }
        }
    }
}
