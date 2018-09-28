package dev.danielholmberg.improve;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import dev.danielholmberg.improve.Components.Tag;
import dev.danielholmberg.improve.Fragments.ArchivedNotesFragment;
import dev.danielholmberg.improve.Fragments.ContactsFragment;
import dev.danielholmberg.improve.Fragments.NotesFragment;
import dev.danielholmberg.improve.Managers.AuthManager;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;

/**
 * Created by Daniel Holmberg.
 */

public class Improve extends Application implements Serializable {

    private AuthManager authManager;
    private FirebaseDatabaseManager firebaseDatabaseManager;

    // volatile attribute makes the singleton thread safe.
    private static volatile Improve sImproveInstance;

    private File rootDir;

    private NotesFragment notesFragmentRef;
    private ContactsFragment contactsFragmentRef;
    private ArchivedNotesFragment archivedNotesFragmentRef;

    private HashMap<String, Tag> tagHashMap = new HashMap<>();

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
        firebaseDatabaseManager = new FirebaseDatabaseManager();

        try {
            rootDir = new File(Environment.getExternalStorageDirectory(), "Improve");
            if(!rootDir.exists()) {
                rootDir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the Firebase authentication manager.
     * @return - Authentication manager of Firebase
     */
    public AuthManager getAuthManager() {
        return this.authManager;
    }

    /**
     * Returns the application specific Firebase database manager.
     * @return - Database manager of Firebase
     */
    public FirebaseDatabaseManager getFirebaseDatabaseManager() {
        return this.firebaseDatabaseManager;
    }

    /**
     * Returns the application specific Root directory.
     * @return - Root directory of application
     */
    public File getRootDir() {
        return this.rootDir;
    }

    // ---- Tag-specific functions ---- //

    /**
     * Sets the HashMap containing all Tags stored in Firebase.
     * @param tagHashMap - New HashMap to replace the old one
     */
    public void setTagHashMap(HashMap<String,Tag> tagHashMap) {
        this.tagHashMap = tagHashMap;
    }

    /**
     * Returns the HashMap containing all Tags stored in Firebase.
     * @return
     */
    public HashMap<String, Tag> getTagHashMap() {
        return tagHashMap;
    }

    /**
     * Returns the Tag with incoming tag-id.
     * @param tagId - The id of the Tag to return
     * @return
     */
    public Tag getTag(String tagId) {
        return tagHashMap.get(tagId);
    }

    /**
     * Adds a Tag to the HashMap.
     * @param tag - The Tag to add
     */
    public void addTagToList(Tag tag) {
        this.tagHashMap.put(tag.getTagId(), tag);
    }

    // ---- Fragment references to use with Snackbar context ---- //

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

}