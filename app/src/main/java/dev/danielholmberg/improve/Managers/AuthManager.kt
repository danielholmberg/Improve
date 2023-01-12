package dev.danielholmberg.improve.Managers

import android.util.Log
import dev.danielholmberg.improve.Improve.Companion.instance
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import dev.danielholmberg.improve.Callbacks.FirebaseAuthCallback
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ServerValue
import com.google.firebase.database.DatabaseError

/**
 * Created by Daniel Holmberg.
 */
class AuthManager {
    var googleSignInClient: GoogleSignInClient? = null

    init {
        // Firebase Instance
        Companion.fireAuth = FirebaseAuth.getInstance()
    }

    val fireAuth: FirebaseAuth
        get() = Companion.fireAuth
    val currentUser: FirebaseUser?
        get() = fireAuth.currentUser
    val currentUserId: String?
        get() = if (currentUser != null) {
            currentUser!!.uid
        } else {
            null
        }
    
    fun signInAnonymously(callback: FirebaseAuthCallback) {
        Companion.fireAuth.signInAnonymously()
            .addOnSuccessListener {
                Log.d(TAG, "*** Successfully signed in to Firebase with chosen Google account ***")
                setUserPresence(callback)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "!!! Failed to sign in to Firebase with chosen Google account:$e")
                // Crashlytics.log("Failed to Sign in Google-user: " + e.toString());
                callback.onFailure(e.toString())
            }
    }

    /**
     * Called when the user has chosen a Google-account for sign in.
     * @param account - GoogleSignInAccount
     */
    fun authGoogleAccountWithFirebase(
        account: GoogleSignInAccount,
        callback: FirebaseAuthCallback
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        Companion.fireAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                Log.d(TAG, "*** Successfully signed in to Firebase with chosen Google account ***")
                setUserPresence(callback)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "!!! Failed to sign in to Firebase with chosen Google account:$e")
                // Crashlytics.log("Failed to Sign in Google-user: " + e.toString());
                callback.onFailure(e.toString())
            }
    }

    /**
     * Listener for users presence to know if user is connected or not
     * and to set a timestamp of the current sign in.
     */
    private fun setUserPresence(callback: FirebaseAuthCallback) {
        val userRef = currentUser!!.uid
        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        val database = FirebaseDatabase.getInstance()
        val myConnectionsRef = database.getReference("users/$userRef/connections")

        // stores the timestamp of my last disconnect (the last time I was seen online)
        val lastOnlineRef = database.getReference("/users/$userRef/lastOnline")
        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java)!!
                if (connected) {
                    val con = myConnectionsRef.push()

                    // when this device disconnects, remove it
                    con.onDisconnect().removeValue()

                    // when I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP)

                    // add this device to my connections list
                    // this value could contain info about the device or a timestamp too
                    con.setValue(java.lang.Boolean.TRUE, ServerValue.TIMESTAMP)
                    Log.d(TAG, "*** Done connecting Google account: $userRef to Firebase ***")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Listener was cancelled at .info/connected: $error")
                // Crashlytics.log("Failed to SetUserPresence: " + error.toString());
            }
        })
        callback.onSuccess()
    }

    fun signOutGoogleAccount(callback: FirebaseAuthCallback) {
        googleSignInClient!!.signOut()
            .addOnSuccessListener {
                Log.d(TAG, "*** Successfully Signed out Google Account")
                Companion.fireAuth.signOut()
                callback.onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to Sign out Google-user: $e")
                // Crashlytics.log("Failed to Sign out Google-user: " + e.toString());
                callback.onFailure(e.toString())
            }
    }

    fun signOutAnonymousAccount(callback: FirebaseAuthCallback) {
        // Remove UserId in Database.
        instance!!.databaseManager!!.userRef.removeValue()
        Companion.fireAuth.signOut()
        callback.onSuccess()
    }

    fun linkAccount(account: GoogleSignInAccount, callback: FirebaseAuthCallback) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        currentUser!!.linkWithCredential(credential)
            .addOnSuccessListener {
                setUserPresence(callback)
                callback.onSuccess()
            }.addOnFailureListener { e ->
                Log.e(TAG, "Failed to Link with Credential: $e")
                // Crashlytics.log("Failed to Link with Credential: " + e.toString());
                callback.onFailure(e.toString())
            }
    }

    companion object {
        private val TAG = AuthManager::class.java.simpleName
        private lateinit var fireAuth: FirebaseAuth
    }
}