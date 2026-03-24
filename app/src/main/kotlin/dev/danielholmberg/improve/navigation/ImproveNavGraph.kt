package dev.danielholmberg.improve.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.danielholmberg.improve.feature.auth.ui.screen.SignInScreen
import dev.danielholmberg.improve.feature.contacts.ui.screen.AddEditContactScreen
import dev.danielholmberg.improve.feature.contacts.ui.screen.CompanyDetailScreen
import dev.danielholmberg.improve.feature.contacts.ui.screen.ContactDetailScreen
import dev.danielholmberg.improve.feature.contacts.ui.screen.ContactsScreen
import dev.danielholmberg.improve.feature.notes.ui.screen.AddEditNoteScreen
import dev.danielholmberg.improve.feature.notes.ui.screen.ArchivedNotesScreen
import dev.danielholmberg.improve.feature.notes.ui.screen.NoteDetailScreen
import dev.danielholmberg.improve.feature.notes.ui.screen.NotesScreen
import dev.danielholmberg.improve.feature.settings.ui.screen.FeedbackScreen
import dev.danielholmberg.improve.feature.settings.ui.screen.PrivacyPolicyScreen
import dev.danielholmberg.improve.feature.settings.ui.screen.SettingsScreen

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Notes, "Notes", Icons.Default.Note),
    BottomNavItem(Screen.Contacts, "Contacts", Icons.Default.Contacts),
    BottomNavItem(Screen.Settings, "Settings", Icons.Default.Settings),
)

@Composable
fun ImproveNavGraph(
    startDestination: String = Screen.SignIn.route,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth
        composable(Screen.SignIn.route) {
            SignInScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Notes.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        // Main scaffold with bottom nav
        composable(Screen.Notes.route) {
            MainScaffold(
                navController = navController,
                currentRoute = Screen.Notes.route
            ) {
                NotesGraph(navController)
            }
        }
        composable(Screen.Contacts.route) {
            MainScaffold(
                navController = navController,
                currentRoute = Screen.Contacts.route
            ) {
                ContactsGraph(navController)
            }
        }
        composable(Screen.Settings.route) {
            MainScaffold(
                navController = navController,
                currentRoute = Screen.Settings.route
            ) {
                SettingsGraph(navController)
            }
        }

        // Notes sub-screens (outside scaffold to hide bottom nav)
        composable(Screen.ArchivedNotes.route) {
            ArchivedNotesScreen(onNavigateBack = { navController.popBackStack() })
        }
        // AddNote — noteId comes via SavedStateHandle (no argument, ViewModel defaults to add mode)
        composable(Screen.AddNote.route) {
            AddEditNoteScreen(onNavigateBack = { navController.popBackStack() })
        }
        // EditNote — noteId in route arg flows automatically into SavedStateHandle
        composable(
            route = Screen.EditNote.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) {
            AddEditNoteScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            NoteDetailScreen(
                noteId = backStackEntry.arguments?.getString("noteId") ?: "",
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { noteId ->
                    navController.navigate(Screen.EditNote.createRoute(noteId))
                }
            )
        }

        // Contacts sub-screens — IDs flow via SavedStateHandle
        composable(Screen.AddContact.route) {
            AddEditContactScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.EditContact.route,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) {
            AddEditContactScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.ContactDetail.route,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) {
            ContactDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { contactId ->
                    navController.navigate(Screen.EditContact.createRoute(contactId))
                }
            )
        }
        composable(
            route = Screen.CompanyDetail.route,
            arguments = listOf(navArgument("companyId") { type = NavType.StringType })
        ) {
            CompanyDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToContactDetail = { contactId ->
                    navController.navigate(Screen.ContactDetail.createRoute(contactId))
                }
            )
        }

        // Settings sub-screens
        composable(Screen.Feedback.route) {
            FeedbackScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    currentRoute: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            if (currentRoute != item.screen.route) {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(padding)
        ) {
            content()
        }
    }
}

@Composable
private fun NotesGraph(navController: NavHostController) {
    NotesScreen(
        onNavigateToAddNote = { navController.navigate(Screen.AddNote.route) },
        onNavigateToNoteDetail = { noteId ->
            navController.navigate(Screen.NoteDetail.createRoute(noteId))
        },
        onNavigateToArchive = { navController.navigate(Screen.ArchivedNotes.route) }
    )
}

@Composable
private fun ContactsGraph(navController: NavHostController) {
    ContactsScreen(
        onNavigateToContactDetail = { contactId ->
            navController.navigate(Screen.ContactDetail.createRoute(contactId))
        },
        onNavigateToAddContact = { navController.navigate(Screen.AddContact.route) },
        onNavigateToAddCompany = { navController.navigate(Screen.AddContact.route) }
    )
}

@Composable
private fun SettingsGraph(navController: NavHostController) {
    SettingsScreen(
        onNavigateToFeedback = { navController.navigate(Screen.Feedback.route) },
        onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
        onNavigateToSignIn = {
            navController.navigate(Screen.SignIn.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    )
}
