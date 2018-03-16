package dev.danielholmberg.improve;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;

import dev.danielholmberg.improve.Managers.AuthManager;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;

/**
 * Created by Daniel Holmberg.
 */

public class Improve extends Application implements Serializable {

    private AuthManager authManager;
    private FirebaseStorageManager firebaseStorageManager;

    // volatile attribute makes the singleton thread safe.
    private static volatile Improve sImproveInstance;

    public static Improve getInstance() {
        // Double checks locking to prevent unnecessary sync.
        if (sImproveInstance == null) {
            synchronized (Improve.class) {
                // If there is no instance available... create new one
                if (sImproveInstance == null) {
                    sImproveInstance = new Improve();
                }
            }
        }
        return sImproveInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sImproveInstance = this;
        // Enabling offline capabilities for Firebase Storage.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Initializing managers.
        authManager = new AuthManager();
        firebaseStorageManager = new FirebaseStorageManager();
    }

    //Make singleton from serialize and deserialize operation.
    protected static Improve readResolve() {
        return getInstance();
    }

    public AuthManager getAuthManager() {
        return this.authManager;
    }

    public FirebaseStorageManager getFirebaseStorageManager() {
        return this.firebaseStorageManager;
    }
}