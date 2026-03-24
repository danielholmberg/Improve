package dev.danielholmberg.improve.feature.contacts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.danielholmberg.improve.domain.model.Company
import dev.danielholmberg.improve.domain.model.Contact
import dev.danielholmberg.improve.domain.usecase.contacts.DeleteCompanyUseCase
import dev.danielholmberg.improve.domain.usecase.contacts.DeleteContactUseCase
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

data class ContactsUiState(
    val companies: List<Company> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
)

sealed interface ContactsUiEvent {
    data class ShowSnackbar(val message: String) : ContactsUiEvent
    data class NavigateToContactDetail(val contactId: String) : ContactsUiEvent
    data object NavigateToAddContact : ContactsUiEvent
    data object NavigateToAddCompany : ContactsUiEvent
}

@HiltViewModel
class ContactsViewModel @Inject constructor(
    getContactsUseCase: GetContactsUseCase,
    getCompaniesUseCase: GetCompaniesUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val deleteCompanyUseCase: DeleteCompanyUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState(isLoading = true))
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ContactsUiEvent>()
    val events: SharedFlow<ContactsUiEvent> = _events.asSharedFlow()

    /**
     * Returns the list of contacts filtered by the current search query.
     * When the query is blank every contact is included.
     */
    val filteredContacts: List<Contact>
        get() {
            val query = _uiState.value.searchQuery.trim()
            return if (query.isBlank()) {
                _uiState.value.contacts
            } else {
                _uiState.value.contacts.filter { contact ->
                    contact.name.contains(query, ignoreCase = true) ||
                        contact.email.contains(query, ignoreCase = true) ||
                        contact.phone.contains(query, ignoreCase = true)
                }
            }
        }

    init {
        viewModelScope.launch {
            combine(
                getCompaniesUseCase(),
                getContactsUseCase(),
            ) { companies, contacts ->
                companies to contacts
            }.collect { (companies, contacts) ->
                _uiState.update { it.copy(companies = companies, contacts = contacts, isLoading = false) }
            }
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            deleteContactUseCase(contact.id, contact.companyId)
                .onSuccess {
                    _events.emit(ContactsUiEvent.ShowSnackbar("Contact deleted"))
                }
                .onFailure { throwable ->
                    _events.emit(
                        ContactsUiEvent.ShowSnackbar(
                            throwable.message ?: "Failed to delete contact",
                        ),
                    )
                }
        }
    }

    fun deleteCompany(company: Company) {
        viewModelScope.launch {
            deleteCompanyUseCase(company.id)
                .onSuccess {
                    _events.emit(ContactsUiEvent.ShowSnackbar("Company deleted"))
                }
                .onFailure { throwable ->
                    _events.emit(
                        ContactsUiEvent.ShowSnackbar(
                            throwable.message ?: "Failed to delete company",
                        ),
                    )
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onAddContactClicked() {
        viewModelScope.launch { _events.emit(ContactsUiEvent.NavigateToAddContact) }
    }

    fun onAddCompanyClicked() {
        viewModelScope.launch { _events.emit(ContactsUiEvent.NavigateToAddCompany) }
    }

    fun onContactClicked(contactId: String) {
        viewModelScope.launch { _events.emit(ContactsUiEvent.NavigateToContactDetail(contactId)) }
    }
}
