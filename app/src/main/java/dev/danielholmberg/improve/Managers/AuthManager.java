package dev.danielholmberg.improve.Managers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import dev.danielholmberg.improve.Callbacks.FirebaseAuthCallback;

/**
 * Created by Daniel Holmberg.
 */

public class AuthManager {
    private static final String TAG = AuthManager.class.getSimpleName();

    private static FirebaseAuth fireAuth;
    private GoogleSignInClient mGoogleSignInClient;

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
     * Called when the user has chosen a Google-account for sign in.
     * @param account - GoogleSignInAccount
     */
    public void authWithFirebase(GoogleSignInAccount account, final FirebaseAuthCallback callback) {
        Log.d(TAG, "Connecting Google account: " + account.getId() + " to Firebase...");

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        fireAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in successful
                            // Add user to Database before going to MainActivity.
                            Log.d(TAG, "*** Successfully signed in to Firebase with chosen Google account ***");
                            setUserPresence(callback);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "!!! Failed to sign in to Firebase with chosen Google account: " + task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "!!! Failed to sign in to Firebase with chosen Google account:" + e);
                        callback.onFailure(e.toString());
                    }
                });
    }

    /**
     * Listener for users presence to know if user is connected or not
     * and to set a timestamp of the current sign in.
     */
    private void setUserPresence(final FirebaseAuthCallback callback) {
        final String userRef = fireAuth.getCurrentUser().getUid();
        // since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myConnectionsRef = database.getReference("users/"+userRef+"/connections");

        // stores the timestamp of my last disconnect (the last time I was seen online)
        final DatabaseReference lastOnlineRef = database.getReference("/users/"+userRef+"/lastOnline");

        final DatabaseReference connectedRef = database.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
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
                    callback.onSuccess();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG,"Listener was cancelled at .info/connected: " + error);
                callback.onFailure(error.toString());
            }
        });
    }

    public void setGoogleSignInClient(GoogleSignInClient mGoogleSignInClient) {
        this.mGoogleSignInClient = mGoogleSignInClient;
    }

    public GoogleSignInClient getmGoogleSignInClient() {
        return this.mGoogleSignInClient;
    }
}
