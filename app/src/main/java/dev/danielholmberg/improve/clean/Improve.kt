package dev.danielholmberg.improve.clean

import android.app.Activity
import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import android.os.Build
import android.app.NotificationManager
import android.app.NotificationChannel
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.feature_note.data.repository.NoteRepositoryImpl
import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository
import dev.danielholmberg.improve.clean.core.SharedPrefsService
import dev.danielholmberg.improve.clean.core.FileService
import dev.danielholmberg.improve.clean.core.GoogleDriveService
import dev.danielholmberg.improve.clean.core.RemoteConfigService
import dev.danielholmberg.improve.clean.feature_authentication.data.repository.AuthRepositoryImpl
import dev.danielholmberg.improve.clean.feature_authentication.data.source.AuthDataSourceImpl
import dev.danielholmberg.improve.clean.feature_authentication.domain.repository.AuthRepository
import dev.danielholmberg.improve.clean.feature_contact.data.repository.CompanyRepositoryImpl
import dev.danielholmberg.improve.clean.feature_contact.data.repository.ContactRepositoryImpl
import dev.danielholmberg.improve.clean.feature_contact.data.source.company.CompanyDataSourceImpl
import dev.danielholmberg.improve.clean.feature_contact.data.source.contact.ContactDataSourceImpl
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Company
import dev.danielholmberg.improve.clean.feature_contact.domain.repository.CompanyRepository
import dev.danielholmberg.improve.clean.feature_contact.domain.repository.ContactRepository
import dev.danielholmberg.improve.clean.feature_contact.presentation.contacts.adapter.CompanyRecyclerViewAdapter
import dev.danielholmberg.improve.clean.feature_contact.presentation.contacts.adapter.ContactRecyclerViewAdapter
import dev.danielholmberg.improve.clean.feature_feedback.data.repository.FeedbackRepositoryImpl
import dev.danielholmberg.improve.clean.feature_feedback.data.source.FeedbackDataSourceImpl
import dev.danielholmberg.improve.clean.feature_feedback.domain.repository.FeedbackRepository
import dev.danielholmberg.improve.clean.feature_note.data.repository.ImageRepositoryImpl
import dev.danielholmberg.improve.clean.feature_note.data.repository.TagRepositoryImpl
import dev.danielholmberg.improve.clean.feature_note.data.source.image.ImageDataSourceImpl
import dev.danielholmberg.improve.clean.feature_note.data.source.note.NoteDataSourceImpl
import dev.danielholmberg.improve.clean.feature_note.data.source.tag.TagDataSourceImpl
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.domain.model.Tag
import dev.danielholmberg.improve.clean.feature_note.domain.repository.ImageRepository
import dev.danielholmberg.improve.clean.feature_note.domain.repository.TagRepository
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.ArchivedNotesAdapter
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.NotesAdapter
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.TagsAdapter
import dev.danielholmberg.improve.clean.feature_privacy_policy.data.repository.PrivacyPolicyRepositoryImpl
import dev.danielholmberg.improve.clean.feature_privacy_policy.data.source.PrivacyPolicyDataSourceImpl
import dev.danielholmberg.improve.clean.feature_privacy_policy.domain.repository.PrivacyPolicyRepository
import java.util.HashMap
import kotlin.jvm.Volatile

class Improve : Application() {

    lateinit var authService: FirebaseAuth
    lateinit var databaseService: FirebaseDatabase
    lateinit var storageService: FirebaseStorage
    lateinit var remoteConfigService: RemoteConfigService

    lateinit var authRepository: AuthRepository
    lateinit var tagRepository: TagRepository
    lateinit var imageRepository: ImageRepository
    lateinit var noteRepository: NoteRepository
    lateinit var feedbackRepository: FeedbackRepository
    lateinit var companyRepository: CompanyRepository
    lateinit var contactRepository: ContactRepository
    lateinit var privacyPolicyRepository: PrivacyPolicyRepository

    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var googleDriveService: GoogleDriveService

    lateinit var fileService: FileService
    lateinit var sharedPrefsService: SharedPrefsService

    var notesAdapter: NotesAdapter? = null
    var archivedNotesAdapter: ArchivedNotesAdapter? = null
    var tagsAdapter: TagsAdapter? = null
    var companyRecyclerViewAdapter: CompanyRecyclerViewAdapter? = null
    private val contactAdapters = HashMap<String, ContactRecyclerViewAdapter>()

    val notes: HashMap<String?, Note>
        get() = notesAdapter!!.hashMap
    val archivedNotes: HashMap<String?, Note>
        get() = archivedNotesAdapter!!.hashMap
    val tags: HashMap<String?, Tag>
        get() = tagsAdapter!!.hashMap
    val companies: HashMap<String?, Company>
        get() = companyRecyclerViewAdapter!!.companiesHashMap

    lateinit var mainActivityRef: MainActivity
    var currentFragment: Fragment? = null
    var isVipUser: Boolean = false

    override fun onCreate() {
        super.onCreate()
        sImproveInstance = this

        createNotificationChannelExport()

        fileService = FileService(this)
        sharedPrefsService = SharedPrefsService(this, "DEVICE_PREFS")

        // Initialize Services

        authService = FirebaseAuth.getInstance()
        databaseService = FirebaseDatabase.getInstance().also { it.setPersistenceEnabled(true) }
        storageService = FirebaseStorage.getInstance()
        remoteConfigService = RemoteConfigService(FirebaseRemoteConfig.getInstance())

        // Configure Google Sign In Client

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize Repositories

        authRepository = AuthRepositoryImpl(
            authDataSource = AuthDataSourceImpl(
                authService = authService,
                googleSignInClient = googleSignInClient,
                database = databaseService
            )
        )
        imageRepository = ImageRepositoryImpl(
            imageDataSource = ImageDataSourceImpl(
                authRepository = authRepository,
                storageService = storageService
            )
        )
        noteRepository = NoteRepositoryImpl(
            noteDataSource = NoteDataSourceImpl(
                authRepository = authRepository,
                databaseService = databaseService
            ),
            imageRepository = imageRepository
        )
        tagRepository = TagRepositoryImpl(
            tagDataSource = TagDataSourceImpl(
                authRepository = authRepository,
                noteRepository = noteRepository,
                databaseService = databaseService
            )
        )
        feedbackRepository = FeedbackRepositoryImpl(
            feedbackDataSource = FeedbackDataSourceImpl(
                databaseService = databaseService
            )
        )
        companyRepository = CompanyRepositoryImpl(
            companyDataSource = CompanyDataSourceImpl(
                authRepository = authRepository,
                databaseService = databaseService
            )
        )
        contactRepository = ContactRepositoryImpl(
            contactDataSource = ContactDataSourceImpl(
                companyRepository = companyRepository
            )
        )
        privacyPolicyRepository = PrivacyPolicyRepositoryImpl(
            privatePolicyDataSource = PrivacyPolicyDataSourceImpl(
                remoteConfigService = remoteConfigService
            )
        )
    }

    fun initDriveService() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val credential = GoogleAccountCredential.usingOAuth2(
                this, setOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account
            val googleDriveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential
            )
                .setApplicationName(resources.getString(R.string.app_name))
                .build()
            this.googleDriveService = GoogleDriveService(googleDriveService)
        } else {
            Log.e(TAG, "Failed to initialize GoogleDriveService!")
        }
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

    fun deleteAnonymousAccountData() {
        authRepository.deleteAccountData(authService.currentUser!!.uid)
    }

    /**
     * Starts an Intent to get the users Google-account to be used to sign in.
     */
    fun startGoogleSignIn(activity: Activity, requestCode: Int) {
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, requestCode)
    }

    fun addContactsAdapter(nameId: String, contactsAdapter: ContactRecyclerViewAdapter) {
        contactAdapters[nameId] = contactsAdapter
    }

    fun getCompanyContactsAdapter(companyId: String): ContactRecyclerViewAdapter? {
        return contactAdapters[companyId]
    }

    fun saveState() {
        tagsAdapter?.let { tagRepository.saveTags(tags) }
        notesAdapter?.let { noteRepository.saveNotes(notes) }
        archivedNotesAdapter?.let { noteRepository.saveArchivedNotes(archivedNotes) }
        companyRecyclerViewAdapter?.let { companyRepository.saveCompanies(companies) }
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