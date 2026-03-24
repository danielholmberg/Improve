package dev.danielholmberg.improve.feature.contacts.viewmodel

import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.danielholmberg.improve.domain.model.Company
import dev.danielholmberg.improve.domain.model.Contact
import dev.danielholmberg.improve.domain.usecase.contacts.AddCompanyUseCase
import dev.danielholmberg.improve.domain.usecase.contacts.AddContactUseCase
import dev.danielholmberg.improve.domain.usecase.contacts.GetCompaniesUseCase
import dev.danielholmberg.improve.domain.usecase.contacts.GetContactsUseCase
import dev.danielholmberg.improve.domain.usecase.contacts.UpdateContactUseCase
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

data class AddEditContactUiState(
    val contactId: String = "",
    val name: String = "",
    val companyId: String = "",
    val email: String = "",
    val phone: String = "",
    val comment: String = "",
    val availableCompanies: List<Company> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
)

sealed interface AddEditContactUiEvent {
    data object NavigateBack : AddEditContactUiEvent
    data class ShowSnackbar(val message: String) : AddEditContactUiEvent
}

@HiltViewModel
class AddEditContactViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getContactsUseCase: GetContactsUseCase,
    getCompaniesUseCase: GetCompaniesUseCase,
    private val addContactUseCase: AddContactUseCase,
    private val updateContactUseCase: UpdateContactUseCase,
    private val addCompanyUseCase: AddCompanyUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditContactUiState(isLoading = true))
    val uiState: StateFlow<AddEditContactUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddEditContactUiEvent>()
    val events: SharedFlow<AddEditContactUiEvent> = _events.asSharedFlow()

    /** True when editing an existing contact, false when adding a new one. */
    val isEditing: Boolean get() = _uiState.value.contactId.isNotBlank()

    init {
        val contactId: String = savedStateHandle["contactId"] ?: ""
        viewModelScope.launch {
            val companies = getCompaniesUseCase().first()
            if (contactId.isNotBlank()) {
                val contact = getContactsUseCase().first().find { it.id == contactId }
                if (contact != null) {
                    _uiState.update {
                        it.copy(
                            contactId = contact.id,
                            name = contact.name,
                            companyId = contact.companyId,
                            email = contact.email,
                            phone = contact.phone,
                            comment = contact.comment,
                            availableCompanies = companies,
                            isLoading = false,
                        )
                    }
                } else {
                    _uiState.update { it.copy(availableCompanies = companies, isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(availableCompanies = companies, isLoading = false) }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onCompanyChange(companyId: String) {
        _uiState.update { it.copy(companyId = companyId) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone, phoneError = null) }
    }

    fun onCommentChange(comment: String) {
        _uiState.update { it.copy(comment = comment) }
    }

    fun saveContact() {
        val state = _uiState.value
        var hasError = false

        val nameError = if (state.name.isBlank()) {
            hasError = true
            "Name is required"
        } else {
            null
        }

        val emailError = if (state.email.isNotBlank() &&
            !Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
        ) {
            hasError = true
            "Enter a valid email address"
        } else {
            null
        }

        val phoneError = if (state.phone.isNotBlank() &&
            !Patterns.PHONE.matcher(state.phone).matches()
        ) {
            hasError = true
            "Enter a valid phone number"
        } else {
            null
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    phoneError = phoneError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val contact = Contact(
                id = state.contactId,
                name = state.name.trim(),
                companyId = state.companyId,
                email = state.email.trim(),
                phone = state.phone.trim(),
                comment = state.comment.trim(),
            )

            val result = if (isEditing) {
                updateContactUseCase(contact)
            } else {
                addContactUseCase(contact)
            }

            result
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                    _events.emit(AddEditContactUiEvent.NavigateBack)
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(
                        AddEditContactUiEvent.ShowSnackbar(
                            throwable.message ?: "Failed to save contact",
                        ),
                    )
                }
        }
    }

    fun addNewCompany(companyName: String) {
        if (companyName.isBlank()) return
        viewModelScope.launch {
            val company = Company(name = companyName.trim())
            addCompanyUseCase(company)
                .onSuccess { newCompany ->
                    val updated = _uiState.value.availableCompanies + company
                    _uiState.update { it.copy(availableCompanies = updated, companyId = company.id) }
                }
                .onFailure { throwable ->
                    _events.emit(
                        AddEditContactUiEvent.ShowSnackbar(
                            throwable.message ?: "Failed to add company",
                        ),
                    )
                }
        }
    }
}
