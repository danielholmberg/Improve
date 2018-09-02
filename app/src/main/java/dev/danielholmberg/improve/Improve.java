package dev.danielholmberg.improve;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.Serializable;

import dev.danielholmberg.improve.Activities.MainActivity;
import dev.danielholmberg.improve.Fragments.ArchivedNotesFragment;
import dev.danielholmberg.improve.Fragments.ContactsFragment;
import dev.danielholmberg.improve.Fragments.NotesFragment;
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

    private File rootDir;

    private NotesFragment notesFragmentRef;
    private ContactsFragment contactsFragmentRef;
    private ArchivedNotesFragment archivedNotesFragmentRef;

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

        try {
            rootDir = new File(Environment.getExternalStorageDirectory(), "Improve");
            if(!rootDir.exists()) {
                rootDir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void setNotesFragmentRef(NotesFragment notesFragmentRef) {
        this.notesFragmentRef = notesFragmentRef;
    }

    public NotesFragment getNotesFragmentRef() {
        return this.notesFragmentRef;
    }
    
    public void setContactFragmentRef(ContactsFragment contactFragmentRef) {
        this.contactsFragmentRef = contactFragmentRef;
    }

    public ContactsFragment getContactsFragmentRef() {
        return this.contactsFragmentRef;
    }

    public void setArchivedNotesFragmentRef(ArchivedNotesFragment archivedNotesFragmentRef) {
        this.archivedNotesFragmentRef = archivedNotesFragmentRef;
    }

    public ArchivedNotesFragment getArchivedNotesFragmentRef() {
        return this.archivedNotesFragmentRef;
    }

    public File getRootDir() {
        return this.rootDir;
    }
}