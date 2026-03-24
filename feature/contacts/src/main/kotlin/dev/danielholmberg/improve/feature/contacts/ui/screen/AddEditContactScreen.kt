package dev.danielholmberg.improve.feature.contacts.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.danielholmberg.improve.feature.contacts.viewmodel.AddEditContactUiEvent
import dev.danielholmberg.improve.feature.contacts.viewmodel.AddEditContactViewModel
import dev.danielholmberg.improve.ui.component.ImproveSnackbarHost
import dev.danielholmberg.improve.ui.component.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditContactViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AddEditContactUiEvent.NavigateBack -> onNavigateBack()
                is AddEditContactUiEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { ImproveSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(if (viewModel.isEditing) "Edit Contact" else "Add Contact")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Name (required)
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                    shape = MaterialTheme.shapes.medium,
                )

                // Company dropdown
                CompanyDropdown(
                    selectedCompanyId = uiState.companyId,
                    companies = uiState.availableCompanies.map { it.id to it.name },
                    onCompanySelected = viewModel::onCompanyChange,
                    onAddNewCompany = viewModel::addNewCompany,
                )

                // Email
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.emailError != null,
                    supportingText = uiState.emailError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    shape = MaterialTheme.shapes.medium,
                )

                // Phone
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.phoneError != null,
                    supportingText = uiState.phoneError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next,
                    ),
                    shape = MaterialTheme.shapes.medium,
                )

                // Comment
                OutlinedTextField(
                    value = uiState.comment,
                    onValueChange = viewModel::onCommentChange,
                    label = { Text("Comment") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    shape = MaterialTheme.shapes.medium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = viewModel::saveContact,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(
                        text = if (viewModel.isEditing) "Save Changes" else "Add Contact",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompanyDropdown(
    selectedCompanyId: String,
    companies: List<Pair<String, String>>,
    onCompanySelected: (String) -> Unit,
    onAddNewCompany: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var newCompanyInput by rememberSaveable { mutableStateOf("") }
    var showNewCompanyField by rememberSaveable { mutableStateOf(false) }

    val selectedName = companies.find { it.first == selectedCompanyId }?.second ?: ""

    Column(modifier = modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = selectedName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Company") },
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = MaterialTheme.shapes.medium,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("None") },
                    onClick = {
                        onCompanySelected("")
                        expanded = false
                    },
                )
                companies.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onCompanySelected(id)
                            expanded = false
                        },
                    )
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            "+ Add new company",
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = {
                        expanded = false
                        showNewCompanyField = true
                    },
                )
            }
        }

        if (showNewCompanyField) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newCompanyInput,
                onValueChange = { newCompanyInput = it },
                label = { Text("New company name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (newCompanyInput.isNotBlank()) {
                                onAddNewCompany(newCompanyInput.trim())
                                newCompanyInput = ""
                                showNewCompanyField = false
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Confirm",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        }
    }
}
