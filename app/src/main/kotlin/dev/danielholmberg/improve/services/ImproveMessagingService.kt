package dev.danielholmberg.improve.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles incoming Firebase Cloud Messaging messages.
 * Currently, a no-op stub — extend for push notification handling.
 */
class ImproveMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // TODO: handle notification payload
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: send token to server if needed
    }
}
