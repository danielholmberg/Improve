package dev.danielholmberg.improve.Services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.danielholmberg.improve.Improve.Companion.instance
import dev.danielholmberg.improve.Managers.DatabaseManager

class NotificationService : FirebaseMessagingService() {

    private val databaseManager: DatabaseManager = instance!!.databaseManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        databaseManager.updateNotificationToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO: Handle FCM messages here.

        // If the application is in the foreground, handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, "From: " + remoteMessage.from)
        Log.d(
            TAG, "Notification Message Title: " + remoteMessage.notification!!
                .title
        )
        Log.d(
            TAG, "Notification Message Body: " + remoteMessage.notification!!
                .body
        )
    }

    companion object {
        private val TAG = NotificationService::class.java.simpleName
    }
}