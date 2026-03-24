package dev.danielholmberg.improve.navigation

sealed class Screen(val route: String) {

    // Auth
    data object SignIn : Screen("sign_in")

    // Bottom nav roots
    data object Notes : Screen("notes")
    data object Contacts : Screen("contacts")
    data object Settings : Screen("settings")

    // Notes sub-screens
    data object AddNote : Screen("add_note")
    data object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: String) = "edit_note/$noteId"
    }
    data object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: String) = "note_detail/$noteId"
    }
    data object ArchivedNotes : Screen("archived_notes")

    // Contacts sub-screens
    data object AddContact : Screen("add_contact")
    data object EditContact : Screen("edit_contact/{contactId}") {
        fun createRoute(contactId: String) = "edit_contact/$contactId"
    }
    data object ContactDetail : Screen("contact_detail/{contactId}") {
        fun createRoute(contactId: String) = "contact_detail/$contactId"
    }
    data object CompanyDetail : Screen("company_detail/{companyId}") {
        fun createRoute(companyId: String) = "company_detail/$companyId"
    }

    // Settings sub-screens
    data object Feedback : Screen("feedback")
    data object PrivacyPolicy : Screen("privacy_policy")
}
