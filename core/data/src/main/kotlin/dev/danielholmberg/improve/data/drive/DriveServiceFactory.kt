package dev.danielholmberg.improve.data.drive

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveServiceFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth
) {

    fun create(): Drive {
        val firebaseUser = requireNotNull(auth.currentUser) { "User not authenticated" }
        val email = requireNotNull(firebaseUser.email) { "User email not available" }

        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        ).also { it.selectedAccountName = email }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Improve")
            .build()
    }
}
