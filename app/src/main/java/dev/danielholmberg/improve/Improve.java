package dev.danielholmberg.improve;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Environment;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import dev.danielholmberg.improve.Activities.MainActivity;
import dev.danielholmberg.improve.Adapters.ArchivedNotesAdapter;
import dev.danielholmberg.improve.Adapters.CompanyRecyclerViewAdapter;
import dev.danielholmberg.improve.Adapters.ContactRecyclerViewAdapter;
import dev.danielholmberg.improve.Adapters.NotesAdapter;
import dev.danielholmberg.improve.Adapters.TagsAdapter;
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
    private FirebaseRemoteConfig firebaseRemoteConfig;

    // volatile attribute makes the singleton thread safe.
    private static volatile Improve sImproveInstance;

    private File rootDir;

    public MainActivity mainActivityRef;

    private NotesFragment notesFragmentRef;
    private ContactsFragment contactsFragmentRef;
    private ArchivedNotesFragment archivedNotesFragmentRef;
    
    private NotesAdapter notesAdapter;
    private ArchivedNotesAdapter archivedNotesAdapter;
    private TagsAdapter tagsAdapter;
    private Fragment currentFragment;
    private CompanyRecyclerViewAdapter companyRecyclerViewAdapter;
    private final HashMap<String, ContactRecyclerViewAdapter> contactAdapters = new HashMap<>();

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

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(BuildConfig.DEBUG ? 0 : 3600)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        try {
            rootDir = new File(Environment.getExternalStorageDirectory(), "Improve");
            if(!rootDir.exists()) {
                rootDir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        createNotificationChannelExport();
    }

    private void createNotificationChannelExport() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = getString(R.string.export_channel_id);
            CharSequence name = getString(R.string.export_channel_name);
            String description = getString(R.string.export_channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
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
     * Returns the application specific Firebase remote config.
     * @return - FirebaseRemoteConfig singleton object.
     */
    public FirebaseRemoteConfig getFirebaseRemoteConfig() {
        return this.firebaseRemoteConfig;
    }

    public void saveState() {
        if(getTagsAdapter() != null) {
            getFirebaseDatabaseManager().saveTags(getTags());
        }
        if(getNotesAdapter() != null) {
            getFirebaseDatabaseManager().saveNotes(getNotes());
        }
        if(getArchivedNotesAdapter() != null) {
            getFirebaseDatabaseManager().saveArchivedNotes(getArchivedNotes());
        }
        if(getCompanyRecyclerViewAdapter() != null) {
            getFirebaseDatabaseManager().saveCompanies(getCompanies());
        }
    }

    /**
     * Returns the application specific Root directory.
     * @return - Root directory of application
     */
    public File getRootDir() {
        return this.rootDir;
    }

    public MainActivity getMainActivityRef() {
        return mainActivityRef;
    }

    public void setMainActivityRef(MainActivity mainActivity) {
        this.mainActivityRef = mainActivity;
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    public void setCurrentFragment(Fragment currentFragment) {
        this.currentFragment = currentFragment;
    }

    // ---- Note functions ---- //

    public void setNotesFragmentRef(NotesFragment notesFragmentRef) {
        this.notesFragmentRef = notesFragmentRef;
    }

    public NotesFragment getNotesFragmentRef() {
        return this.notesFragmentRef;
    }

    public void setNotesAdapter(NotesAdapter notesAdapter) {
        this.notesAdapter = notesAdapter;
    }

    public NotesAdapter getNotesAdapter() {
        return notesAdapter;
    }

    public HashMap<String, Object> getNotes() {
        return getNotesAdapter().getHashMap();
    }

    // ---- Archived note functions ---- //

    public void setArchivedNotesFragmentRef(ArchivedNotesFragment archivedNotesFragmentRef) {
        this.archivedNotesFragmentRef = archivedNotesFragmentRef;
    }

    public ArchivedNotesFragment getArchivedNotesFragmentRef() {
        return this.archivedNotesFragmentRef;
    }

    public void setArchivedNotesAdapter(ArchivedNotesAdapter archivedNotesAdapter) {
        this.archivedNotesAdapter = archivedNotesAdapter;
    }

    public ArchivedNotesAdapter getArchivedNotesAdapter() {
        return archivedNotesAdapter;
    }

    public HashMap<String,Object> getArchivedNotes() {
        return getArchivedNotesAdapter().getHashMap();
    }

    // ---- Tag functions ---- //

    public void setTagsAdapter(TagsAdapter tagsAdapter) {
        this.tagsAdapter = tagsAdapter;
    }

    public TagsAdapter getTagsAdapter() {
        return tagsAdapter;
    }

    public HashMap<String,Object> getTags() {
        return getTagsAdapter().getHashMap();
    }

    // ---- Contact functions ---- //

    public void setContactFragmentRef(ContactsFragment contactFragmentRef) {
        this.contactsFragmentRef = contactFragmentRef;
    }

    public ContactsFragment getContactsFragmentRef() {
        return this.contactsFragmentRef;
    }

    // ---- Company functions ---- //

    public void setCompanyRecyclerViewAdapter(CompanyRecyclerViewAdapter companyRecyclerViewAdapter) {
        this.companyRecyclerViewAdapter = companyRecyclerViewAdapter;
    }

    public CompanyRecyclerViewAdapter getCompanyRecyclerViewAdapter() {
        return companyRecyclerViewAdapter;
    }

    public HashMap<String, Object> getCompanies() {
        return getCompanyRecyclerViewAdapter().getCompaniesHashMap();
    }

    public void addContactsAdapter(String nameId, ContactRecyclerViewAdapter contactsAdapter) {
        contactAdapters.put(nameId, contactsAdapter);
    }

    public ContactRecyclerViewAdapter getCompanyContactsAdapter(String companyId) {
        return contactAdapters.get(companyId);
    }
}