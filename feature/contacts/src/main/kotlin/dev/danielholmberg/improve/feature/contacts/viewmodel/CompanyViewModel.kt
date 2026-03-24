package dev.danielholmberg.improve.feature.contacts.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.danielholmberg.improve.domain.model.Company
import dev.danielholmberg.improve.domain.model.Contact
import dev.danielholmberg.improve.domain.usecase.contacts.GetCompaniesUseCase
import dev.danielholmberg.improve.domain.usecase.contacts.GetContactsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompanyUiState(
    val company: Company? = null,
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class CompanyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCompaniesUseCase: GetCompaniesUseCase,
    getContactsUseCase: GetContactsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompanyUiState(isLoading = true))
    val uiState: StateFlow<CompanyUiState> = _uiState.asStateFlow()

    init {
        val companyId: String = savedStateHandle["companyId"] ?: ""
        viewModelScope.launch {
            combine(
                getCompaniesUseCase(),
                getContactsUseCase(),
            ) { companies, contacts ->
                val company = companies.find { it.id == companyId }
                val companyContacts = contacts.filter { it.companyId == companyId }
                company to companyContacts
            }.collect { (company, contacts) ->
                _uiState.update {
                    it.copy(
                        company = company,
                        contacts = contacts,
                        isLoading = false,
                    )
                }
            }
        }
    }
}
