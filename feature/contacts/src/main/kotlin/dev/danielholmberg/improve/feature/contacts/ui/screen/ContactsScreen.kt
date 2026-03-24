package dev.danielholmberg.improve.feature.contacts.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.danielholmberg.improve.domain.model.Company
import dev.danielholmberg.improve.domain.model.Contact
import dev.danielholmberg.improve.feature.contacts.ui.component.ContactCard
import dev.danielholmberg.improve.feature.contacts.viewmodel.ContactsUiEvent
import dev.danielholmberg.improve.feature.contacts.viewmodel.ContactsViewModel
import dev.danielholmberg.improve.ui.component.ConfirmDialog
import dev.danielholmberg.improve.ui.component.EmptyState
import dev.danielholmberg.improve.ui.component.ImproveSearchBar
import dev.danielholmberg.improve.ui.component.ImproveSnackbarHost
import dev.danielholmberg.improve.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContactsScreen(
    onNavigateToContactDetail: (String) -> Unit,
    onNavigateToAddContact: () -> Unit,
    onNavigateToAddCompany: () -> Unit,
    viewModel: ContactsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var fabExpanded by rememberSaveable { mutableStateOf(false) }
    var companyToDelete by remember { mutableStateOf<Company?>(null) }
    var contactToDelete by remember { mutableStateOf<Contact?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ContactsUiEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
                is ContactsUiEvent.NavigateToContactDetail ->
                    onNavigateToContactDetail(event.contactId)
                ContactsUiEvent.NavigateToAddContact ->
                    onNavigateToAddContact()
                ContactsUiEvent.NavigateToAddCompany ->
                    onNavigateToAddCompany()
            }
        }
    }

    // Delete-company confirmation dialog
    companyToDelete?.let { company ->
        ConfirmDialog(
            title = "Delete Company",
            message = "Delete \"${company.name}\"? This will not delete associated contacts.",
            confirmText = "Delete",
            onConfirm = {
                viewModel.deleteCompany(company)
                companyToDelete = null
            },
            onDismiss = { companyToDelete = null },
        )
    }

    // Delete-contact confirmation dialog
    contactToDelete?.let { contact ->
        ConfirmDialog(
            title = "Delete Contact",
            message = "Delete \"${contact.name}\"?",
            confirmText = "Delete",
            onConfirm = {
                viewModel.deleteContact(contact)
                contactToDelete = null
            },
            onDismiss = { contactToDelete = null },
        )
    }

    Scaffold(
        snackbarHost = { ImproveSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Contacts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        floatingActionButton = {
            ExpandableFab(
                expanded = fabExpanded,
                onExpandToggle = { fabExpanded = !fabExpanded },
                onAddContact = {
                    fabExpanded = false
                    viewModel.onAddContactClicked()
                },
                onAddCompany = {
                    fabExpanded = false
                    viewModel.onAddCompanyClicked()
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (uiState.isLoading) {
                LoadingIndicator()
                return@Box
            }

            Column(modifier = Modifier.fillMaxSize()) {
                ImproveSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    placeholder = "Search contacts…",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )

                val visibleContacts = viewModel.filteredContacts

                if (visibleContacts.isEmpty() && uiState.contacts.isEmpty()) {
                    EmptyState(
                        message = "No contacts yet.\nTap + to add your first contact.",
                    )
                } else if (visibleContacts.isEmpty()) {
                    EmptyState(message = "No contacts match your search.")
                } else {
                    // Group contacts by companyId; contacts without a company go under a null key.
                    val companyMap = uiState.companies.associateBy { it.id }
                    val grouped: Map<Company?, List<Contact>> = visibleContacts
                        .groupBy { contact ->
                            companyMap[contact.companyId]
                        }
                        .entries
                        .sortedWith(
                            compareBy(
                                nullsLast(),
                                { entry: Map.Entry<Company?, List<Contact>> -> entry.key?.name },
                            ),
                        )
                        .associate { it.key to it.value }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        grouped.forEach { (company, contacts) ->
                            // Section header
                            stickyHeader(key = "header_${company?.id ?: "none"}") {
                                CompanySectionHeader(
                                    company = company,
                                    onLongClick = {
                                        if (company != null) companyToDelete = company
                                    },
                                )
                            }

                            items(
                                items = contacts,
                                key = { it.id },
                            ) { contact ->
                                ContactCard(
                                    contact = contact,
                                    companyName = company?.name,
                                    onClick = { viewModel.onContactClicked(contact.id) },
                                    onLongClick = { contactToDelete = contact },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }

                            item(key = "spacer_${company?.id ?: "none"}") {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Extra bottom padding so FAB doesn't obscure last item
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }

            // Dim scrim when FAB is expanded
            if (fabExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                        .combinedClickable { fabExpanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompanySectionHeader(
    company: Company?,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (company != null) Icons.Default.Business else Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = company?.name ?: "No Company",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExpandableFab(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onAddContact: () -> Unit,
    onAddCompany: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Secondary FAB: Add Company
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
        ) {
            ExtendedFloatingActionButton(
                onClick = onAddCompany,
                icon = {
                    Icon(Icons.Default.Business, contentDescription = null)
                },
                text = { Text("Add Company") },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        // Secondary FAB: Add Contact
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
        ) {
            ExtendedFloatingActionButton(
                onClick = onAddContact,
                icon = {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                },
                text = { Text("Add Contact") },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        // Primary toggle FAB
        FloatingActionButton(
            onClick = onExpandToggle,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "Collapse" else "Add",
            )
        }
    }
}
