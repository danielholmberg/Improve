package dev.danielholmberg.improve

import android.app.Application
import dev.danielholmberg.improve.Managers.AuthManager
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager
import dev.danielholmberg.improve.Managers.FirebaseStorageManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dev.danielholmberg.improve.Services.DriveServiceHelper
import dev.danielholmberg.improve.Activities.MainActivity
import dev.danielholmberg.improve.Fragments.NotesFragment
import dev.danielholmberg.improve.Fragments.ContactsFragment
import dev.danielholmberg.improve.Fragments.ArchivedNotesFragment
import dev.danielholmberg.improve.Adapters.NotesAdapter
import dev.danielholmberg.improve.Adapters.ArchivedNotesAdapter
import dev.danielholmberg.improve.Adapters.TagsAdapter
import dev.danielholmberg.improve.Adapters.CompanyRecyclerViewAdapter
import dev.danielholmberg.improve.Adapters.ContactRecyclerViewAdapter
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import android.os.Build
import android.app.NotificationManager
import android.app.NotificationChannel
import android.util.Log
import androidx.fragment.app.Fragment
import java.io.File
import java.io.Serializable
import java.util.HashMap
import javax.inject.Singleton
import kotlin.jvm.Volatile

/**
 * Created by Daniel Holmberg.
 */
@Singleton
class Improve : Application(), Serializable {

    var authManager: AuthManager? = null
        private set

    var firebaseDatabaseManager: FirebaseDatabaseManager? = null
        private set

    var firebaseStorageManager: FirebaseStorageManager? = null
        private set

    var firebaseRemoteConfig: FirebaseRemoteConfig? = null
        private set

    var driveServiceHelper: DriveServiceHelper? = null

    var mainActivityRef: MainActivity? = null

    private var rootDir: File? = null
    private var imageDir: File? = null

    // ---- Note functions ---- //
    var notesFragmentRef: NotesFragment? = null
    var contactsFragmentRef: ContactsFragment? = null
        private set

    // ---- Archived note functions ---- //
    var archivedNotesFragmentRef: ArchivedNotesFragment? = null
    var notesAdapter: NotesAdapter? = null
    var archivedNotesAdapter: ArchivedNotesAdapter? = null

    // ---- Tag functions ---- //
    var tagsAdapter: TagsAdapter? = null
    var currentFragment: Fragment? = null

    // ---- Company functions ---- //
    var companyRecyclerViewAdapter: CompanyRecyclerViewAdapter? = null
    private val contactAdapters = HashMap<String, ContactRecyclerViewAdapter>()

    @set:JvmName("setVipUser")
    var isVipUser: Boolean = false

    private val vipUsers: Map<String, String> = object : HashMap<String, String>() {
        init {
            put("1", "danielholmberg.dev@gmail.com")
            put("2", "danielkurtholmberg@gmail.com")
        }
    }

    override fun onCreate() {
        super.onCreate()
        sImproveInstance = this

        // Enabling offline capabilities for Firebase Storage.
        // OBS!!! Can create Local Firebase cache issue where data changed in console won't take effect.
        // Reset cache by setting this to FASLE if there is an issue with data out of sync,
        // or a crash due to changed Model parameters.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        // Create Root-Dir if not already existing.
        getRootDir()

        // Initializing managers.
        authManager = AuthManager()
        firebaseDatabaseManager = FirebaseDatabaseManager()
        firebaseStorageManager = FirebaseStorageManager()
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600.toLong())
            .build()
        firebaseRemoteConfig!!.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig!!.setDefaultsAsync(R.xml.remote_config_defaults)
        firebaseRemoteConfig!!.setDefaultsAsync(vipUsers)

        createNotificationChannelExport()
    }

    private fun createNotificationChannelExport() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = getString(R.string.export_channel_id)
            val name: CharSequence = getString(R.string.export_channel_name)
            val description = getString(R.string.export_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun saveState() {
        if (tagsAdapter != null) {
            firebaseDatabaseManager!!.saveTags(tags)
        }
        if (notesAdapter != null) {
            firebaseDatabaseManager!!.saveNotes(notes)
        }
        if (archivedNotesAdapter != null) {
            firebaseDatabaseManager!!.saveArchivedNotes(archivedNotes)
        }
        if (companyRecyclerViewAdapter != null) {
            firebaseDatabaseManager!!.saveCompanies(companies)
        }
    }

    /**
     * Returns the application specific Root directory.
     * @return - Root directory of application
     */
    fun getRootDir(): File? {
        rootDir = applicationContext.filesDir
        if (!rootDir!!.exists()) {
            rootDir!!.mkdirs()
        }
        Log.d("Improve", "RootDir: " + rootDir!!.getPath())
        return rootDir
    }

    fun getImageDir(): File? {
        imageDir = File(rootDir, FirebaseStorageManager.IMAGES_REF)
        if (!imageDir!!.exists()) {
            imageDir!!.mkdirs()
        }
        Log.d("Improve", "ImageDir: " + imageDir!!.path)
        return imageDir
    }

    val notes: HashMap<String, Any>
        get() = notesAdapter!!.hashMap
    val archivedNotes: HashMap<String, Any>
        get() = archivedNotesAdapter!!.hashMap
    val tags: HashMap<String, Any>
        get() = tagsAdapter!!.hashMap

    // ---- Contact functions ---- //
    fun setContactFragmentRef(contactFragmentRef: ContactsFragment?) {
        contactsFragmentRef = contactFragmentRef
    }

    val companies: HashMap<String, Any>
        get() = companyRecyclerViewAdapter!!.companiesHashMap

    fun addContactsAdapter(nameId: String, contactsAdapter: ContactRecyclerViewAdapter) {
        contactAdapters[nameId] = contactsAdapter
    }

    fun getCompanyContactsAdapter(companyId: String): ContactRecyclerViewAdapter? {
        return contactAdapters[companyId]
    }

    companion object {
        // volatile attribute makes the singleton thread safe.
        @Volatile
        private var sImproveInstance: Improve? = null

        @JvmStatic
        val instance: Improve?
            get() {
                // Double checks locking to prevent unnecessary sync.
                if (sImproveInstance == null) {
                    synchronized(Improve::class.java) {
                        // If there is no instance available... create new one
                        if (sImproveInstance == null) {
                            sImproveInstance = Improve()
                        }
                    }
                }
                return sImproveInstance
            }
    }
}