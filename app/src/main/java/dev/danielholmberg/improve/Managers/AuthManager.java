package dev.danielholmberg.improve.Managers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import dev.danielholmberg.improve.Callbacks.FirebaseAuthCallback;
import dev.danielholmberg.improve.Improve;

/**
 * Created by Daniel Holmberg.
 */

public class AuthManager {
    private static final String TAG = AuthManager.class.getSimpleName();

    private static FirebaseAuth fireAuth;
    private GoogleSignInClient googleSignInClient;

    public AuthManager() {
        // Firebase Instance
        fireAuth = FirebaseAuth.getInstance();
    }

    public FirebaseAuth getFireAuth() {
        return fireAuth;
    }

    public FirebaseUser getCurrentUser() {
        return getFireAuth().getCurrentUser();
    }

    public String getCurrentUserId() {
        if(getCurrentUser() != null) {
            return getCurrentUser().getUid();
        } else {
            return null;
        }
    }

    public DatabaseReference getUserRef() {
        return FirebaseDatabase.getInstance().getReference("users/" + getCurrentUserId());
    }

    /**
     * Sign
     */
    public void signInAnonymously(final FirebaseAuthCallback callback) {
        fireAuth.signInAnonymously()
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "*** Successfully signed in to Firebase with chosen Google account ***");
                        setUserPresence(callback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "!!! Failed to sign in to Firebase with chosen Google account:" + e);
                        Crashlytics.log("Failed to Sign in Google-user: " + e.toString());
                        callback.onFailure(e.toString());
                    }
                });
    }

    /**
     * Called when the user has chosen a Google-account for sign in.
     * @param account - GoogleSignInAccount
     */
    public void authGoogleAccountWithFirebase(GoogleSignInAccount account, final FirebaseAuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        fireAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "*** Successfully signed in to Firebase with chosen Google account ***");
                        setUserPresence(callback);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "!!! Failed to sign in to Firebase with chosen Google account:" + e.toString());
                        Crashlytics.log("Failed to Sign in Google-user: " + e.toString());
                        callback.onFailure(e.toString());
                    }
                });
    }

    /**
     * Listener for users presence to know if user is connected or not
     * and to set a timestamp of the current sign in.
     */
    private void setUserPresence(final FirebaseAuthCallback callback) {
        final String userRef = getCurrentUser().getUid();
        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myConnectionsRef = database.getReference("users/"+userRef+"/connections");

        // stores the timestamp of my last disconnect (the last time I was seen online)
        final DatabaseReference lastOnlineRef = database.getReference("/users/"+userRef+"/lastOnline");

        final DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    DatabaseReference con = myConnectionsRef.push();

                    // when this device disconnects, remove it
                    con.onDisconnect().removeValue();

                    // when I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                    // add this device to my connections list
                    // this value could contain info about the device or a timestamp too
                    con.setValue(Boolean.TRUE, ServerValue.TIMESTAMP);

                    Log.d(TAG, "*** Done connecting Google account: " + userRef + " to Firebase ***");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG,"Listener was cancelled at .info/connected: " + error);
                Crashlytics.log("Failed to SetUserPresence: " + error.toString());
            }
        });
        callback.onSuccess();
    }

    public void setGoogleSignInClient(GoogleSignInClient googleSignInClient) {
        this.googleSignInClient = googleSignInClient;
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return this.googleSignInClient;
    }

    public void signOutGoogleAccount(final FirebaseAuthCallback callback) {
        googleSignInClient.signOut()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        fireAuth.signOut();
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.log("Failed to Sign out Google-user: " + e.toString());
                        callback.onFailure(e.toString());
                    }
                });

    }

    public void signOutAnonymousAccount(FirebaseAuthCallback callback) {
        // Remove UserId in Database.
        Improve.getInstance().getFirebaseDatabaseManager().getUserRef().removeValue();
        fireAuth.signOut();
        callback.onSuccess();
    }

    public void linkAccount(GoogleSignInAccount account, final FirebaseAuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        getCurrentUser().linkWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        setUserPresence(callback);
                        callback.onSuccess();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.log("Failed to Link with Credential: " + e.toString());
                        callback.onFailure(e.toString());
                    }
                });
    }
}
