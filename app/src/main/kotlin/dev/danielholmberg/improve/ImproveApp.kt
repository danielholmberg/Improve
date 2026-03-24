package dev.danielholmberg.improve

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ImproveApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Enable Firebase offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        // Create notification channel for exports (Android O+)
        createExportNotificationChannel()
    }

    private fun createExportNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                EXPORT_CHANNEL_ID,
                "Export",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for export operations"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val EXPORT_CHANNEL_ID = "export_channel"
    }
}
