package dev.danielholmberg.improve

import android.app.Application
import dev.danielholmberg.improve.Managers.AuthManager
import dev.danielholmberg.improve.Managers.DatabaseManager
import dev.danielholmberg.improve.Managers.StorageManager
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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dev.danielholmberg.improve.Managers.RemoteConfigManager
import dev.danielholmberg.improve.Services.SharedPrefsService
import java.io.File
import java.io.Serializable
import java.util.*
import javax.inject.Singleton
import kotlin.jvm.Volatile

@Singleton
class Improve : Application(), Serializable {

    lateinit var authManager: AuthManager
        private set
    lateinit var databaseManager: DatabaseManager
        private set
    lateinit var storageManager: StorageManager
        private set
    lateinit var remoteConfigManager: RemoteConfigManager
        private set

    var driveServiceHelper: DriveServiceHelper? = null

    var mainActivityRef: MainActivity? = null

    lateinit var imageDir: File
        private set

    var notesFragmentRef: NotesFragment? = null
    var contactsFragmentRef: ContactsFragment? = null

    var archivedNotesFragmentRef: ArchivedNotesFragment? = null
    var notesAdapter: NotesAdapter? = null
    var archivedNotesAdapter: ArchivedNotesAdapter? = null

    var tagsAdapter: TagsAdapter? = null
    var currentFragment: Fragment? = null

    var companyRecyclerViewAdapter: CompanyRecyclerViewAdapter? = null
    private val contactAdapters = HashMap<String, ContactRecyclerViewAdapter>()

    val notes: HashMap<String?, Any>
        get() = notesAdapter!!.hashMap
    val archivedNotes: HashMap<String?, Any>
        get() = archivedNotesAdapter!!.hashMap
    val tags: HashMap<String?, Any>
        get() = tagsAdapter!!.hashMap
    val companies: HashMap<String, Any>
        get() = companyRecyclerViewAdapter!!.companiesHashMap

    @set:JvmName("setVipUser")
    var isVipUser: Boolean = false

    private val vipUsers: Map<String, String> = object : HashMap<String, String>() {
        init {
            put("1", "danielholmberg.dev@gmail.com")
            put("2", "danielkurtholmberg@gmail.com")
        }
    }

    private lateinit var sharedPrefsService: SharedPrefsService
    lateinit var deviceId: String

    override fun onCreate() {
        super.onCreate()
        sImproveInstance = this

        loadPreferences()
        enableFirebaseStorageOfflineCapabilities()

        val rootDir = generateRootDir()
        generateImageDir(rootDir)

        authManager = AuthManager()
        databaseManager = DatabaseManager()
        storageManager = StorageManager()
        remoteConfigManager = RemoteConfigManager(remoteConfig = FirebaseRemoteConfig.getInstance())

        setRemoteConfigSettings()

        createNotificationChannelExport()
    }

    private fun enableFirebaseStorageOfflineCapabilities() {
        // Enabling offline capabilities for Firebase Storage.
        // OBS!!! Can create Local Firebase cache issue where data changed in console won't take effect.
        // Reset cache by setting this to FALSE if there is an issue with data out of sync,
        // or a crash due to changed Model parameters.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    private fun loadPreferences() {
        sharedPrefsService = SharedPrefsService(this, "DEVICE_PREFS")

        // Retrieve the stored Device ID or generate a new
        val deviceIdKey = "device_id"
        val storedDeviceId = sharedPrefsService.getString(deviceIdKey)
        if ((storedDeviceId == null) || storedDeviceId.isEmpty()) {
            deviceId = UUID.randomUUID().toString()
            sharedPrefsService.putString(deviceIdKey, deviceId)
        } else {
            deviceId = storedDeviceId
        }

        Log.i(TAG, "Device ID: $deviceId")
    }

    private fun setRemoteConfigSettings() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600.toLong())
            .build()
        remoteConfigManager.setConfigSettingsAsync(configSettings)
        remoteConfigManager.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfigManager.setDefaultsAsync(vipUsers)
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
        tagsAdapter?.let { databaseManager.saveTags(tags) }
        notesAdapter?.let { databaseManager.saveNotes(notes) }
        archivedNotesAdapter?.let { databaseManager.saveArchivedNotes(archivedNotes) }
        companyRecyclerViewAdapter?.let { databaseManager.saveCompanies(companies) }
    }

    /**
     * Returns the application specific Root directory.
     * @return - Root directory of application
     */
    private fun generateRootDir(): File {
        val rootDir = applicationContext.filesDir
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }
        Log.d("Improve", "RootDir: " + rootDir.path)
        return rootDir
    }

    private fun generateImageDir(rootDir: File): File {
        imageDir = File(rootDir, StorageManager.IMAGES_REF)
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
        Log.d("Improve", "ImageDir: " + imageDir.path)
        return imageDir
    }

    fun addContactsAdapter(nameId: String, contactsAdapter: ContactRecyclerViewAdapter) {
        contactAdapters[nameId] = contactsAdapter
    }

    fun getCompanyContactsAdapter(companyId: String): ContactRecyclerViewAdapter? {
        return contactAdapters[companyId]
    }

    companion object {
        private val TAG = this::class.simpleName

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