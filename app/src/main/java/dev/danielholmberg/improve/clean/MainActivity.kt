package dev.danielholmberg.improve.clean

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.media.SoundPool
import android.os.Bundle
import dev.danielholmberg.improve.R
import com.google.firebase.auth.GoogleAuthProvider
import com.squareup.picasso.Picasso
import dev.danielholmberg.improve.clean.core.util.CircleTransform
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener
import android.content.Intent
import android.media.AudioAttributes
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import dev.danielholmberg.improve.clean.feature_authentication.util.AuthCallback
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.feature_authentication.SignInActivity
import dev.danielholmberg.improve.clean.feature_contact.presentation.contacts.adapter.CompanyRecyclerViewAdapter
import dev.danielholmberg.improve.clean.feature_contact.presentation.fragments.ContactsFragment
import dev.danielholmberg.improve.clean.feature_feedback.presentation.FeedbackActivity
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.fragment.ArchivedNotesFragment
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.fragment.NotesFragment
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.ArchivedNotesAdapter
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.NotesAdapter
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.TagsAdapter
import dev.danielholmberg.improve.clean.feature_privacy_policy.presentation.PrivacyPolicyActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    // flag to load home fragment when currentUser presses back key
    private val shouldLoadHomeFragOnBackPress = true
    private var doubleBackToExitPressedOnce = false
    private var currentUser: FirebaseUser? = null
    private var toolbar: Toolbar? = null
    private var drawer: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private var hasLoadedNavView = false
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var currentQuote: String? = null
    private var quoteTextSwitcher: TextSwitcher? = null
    private val quoteUpdateTime = 20000
    private var soundPool: SoundPool? = null
    private var quoteSound = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        instance!!.mainActivityRef = this
        currentUser = instance!!.authService.currentUser

        // Retrieve VIP_USERS from Firebase RemoteConfig
        instance!!.remoteConfigService.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val updated = task.result
                    Log.d(TAG, "Config params updated: $updated")
                }
                val retrievedVIPUsers = instance!!.remoteConfigService.getVipUsers()
                Log.d(TAG, "Retrieved VIP_USERS: $retrievedVIPUsers")
                if (currentUser!!.email != null && currentUser!!.email!!.isNotEmpty()) {
                    Log.d(TAG, "Current user: " + currentUser!!.email)
                    instance!!.isVipUser = retrievedVIPUsers.contains(currentUser!!.email!!)
                } else {
                    instance!!.isVipUser = false
                }
                initNavDrawer()
            }
        initActivity()
        initData()
        loadCurrentFragment()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(0) {
                handleBackNavigation()
            }
        }
    }

    private fun initActivity() {
        // Initializing the Toolbar
        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        // Initializing NavigationDrawer
        drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer!!.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState()

        initNavDrawer()
        instance!!.initDriveService()
    }

    private fun initData() {
        instance!!.tagsAdapter = TagsAdapter()
        instance!!.notesAdapter = NotesAdapter()
        instance!!.archivedNotesAdapter = ArchivedNotesAdapter()
        instance!!.companyRecyclerViewAdapter = CompanyRecyclerViewAdapter()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (hasLoadedNavView) {
                quoteTextSwitcher!!.visibility = View.GONE
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (hasLoadedNavView) {
                quoteTextSwitcher!!.visibility = View.VISIBLE
            }
        }
    }

    private fun initNavDrawer() {
        // Setting the Image, Name and Email in the NavigationDrawer Header.
        navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val drawerHeaderImageLayout = navigationView!!.getHeaderView(0)
            .findViewById<View>(R.id.drawer_header_image_layout) as RelativeLayout
        val vipUserSymbol =
            navigationView!!.getHeaderView(0).findViewById<View>(R.id.vip_user_symbol) as ImageView
        val drawerHeaderImage = navigationView!!.getHeaderView(0)
            .findViewById<View>(R.id.drawer_header_image_iv) as ImageView
        val drawerHeaderName = navigationView!!.getHeaderView(0)
            .findViewById<View>(R.id.drawer_header_name_tv) as TextView
        val drawerHeaderEmail = navigationView!!.getHeaderView(0)
            .findViewById<View>(R.id.drawer_header_email_tv) as TextView
        if (instance!!.isVipUser) {
            vipUserSymbol.visibility = View.VISIBLE
        } else {
            vipUserSymbol.visibility = View.GONE
        }
        if (currentUser!!.isAnonymous) {
            drawerHeaderImageLayout.visibility = View.GONE
            drawerHeaderEmail.visibility = View.GONE
            drawerHeaderName.visibility = View.GONE
        } else {
            var photoUri = currentUser!!.photoUrl
            val providerData = currentUser!!.providerData
            for (ui in providerData) {
                if (ui.providerId == GoogleAuthProvider.PROVIDER_ID) {
                    photoUri = ui.photoUrl
                    break
                }
            }
            Picasso.get()
                .load(photoUri)
                .error(R.drawable.ic_error_no_photo_white)
                .transform(CircleTransform())
                .resize(200, 200)
                .centerCrop()
                .into(drawerHeaderImage)
            drawerHeaderName.text = currentUser!!.displayName
            drawerHeaderEmail.text = currentUser!!.email
        }

        // Initializing navigation menu
        setUpNavigationView()
        setUpQuoteView()
        hasLoadedNavView = true
    }

    private fun setUpNavigationView() {
        val context: Context = this
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView!!.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { menuItem ->
            // This method will trigger on item Click of navigation menu
            // Check to see which item was being clicked and perform appropriate action
            when (menuItem.itemId) {
                R.id.nav_notes -> {
                    navItemIndex = 0
                    CURRENT_TAG = TAG_NOTES_FRAGMENT
                    loadCurrentFragment()
                }
                R.id.nav_archived_notes -> {
                    navItemIndex = 1
                    CURRENT_TAG = TAG_ARCHIVED_NOTES_FRAGMENT
                    loadCurrentFragment()
                }
                R.id.nav_contacts -> {
                    navItemIndex = 2
                    CURRENT_TAG = TAG_CONTACTS_FRAGMENT
                    loadCurrentFragment()
                }
                R.id.nav_feedback -> {
                    menuItem.isChecked = true
                    drawer!!.closeDrawers()
                    drawer!!.addDrawerListener(object : SimpleDrawerListener() {
                        override fun onDrawerClosed(drawerView: View) {
                            super.onDrawerClosed(drawerView)
                            startActivity(Intent(context, FeedbackActivity::class.java))
                            drawer!!.removeDrawerListener(this)
                        }
                    })
                }
                R.id.nav_privacy_policy -> {
                    menuItem.isChecked = true
                    drawer!!.closeDrawers()
                    drawer!!.addDrawerListener(object : SimpleDrawerListener() {
                        override fun onDrawerClosed(drawerView: View) {
                            super.onDrawerClosed(drawerView)
                            startActivity(Intent(context, PrivacyPolicyActivity::class.java))
                            drawer!!.removeDrawerListener(this)
                        }
                    })
                }
                R.id.nav_sign_out -> {
                    startSignOut()
                    drawer!!.closeDrawers()
                    return@OnNavigationItemSelectedListener true
                }
                else -> navItemIndex = 0
            }
            true
        })

        // Calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle!!.syncState()
    }

    private fun setUpQuoteView() {
        quoteTextSwitcher =
            navigationView!!.findViewById<View>(R.id.nav_view_bottom_quote) as TextSwitcher
        loadAnimations()
        loadQuoteSound()
        val timer = Timer()
        timer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    runOnUiThread { setQuoteText() }
                }
            }, 0, quoteUpdateTime.toLong()
        )
        quoteTextSwitcher!!.setOnClickListener {
            setQuoteText()
            soundPool!!.play(quoteSound, 1f, 1f, 0, 0, 1f)
        }
    }

    private fun loadQuoteSound() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
        quoteSound = soundPool!!.load(this, R.raw.quote_easter_egg, 1)
    }

    private fun setQuoteText() {
        val quotesArray = resources.getStringArray(R.array.quotes)
        currentQuote = quotesArray[Random().nextInt(quotesArray.size)]
        if (currentQuote == null || currentQuote!!.isEmpty()) {
            currentQuote = "There is always room for improvement."
        }
        quoteTextSwitcher!!.setText(currentQuote)
    }

    private fun loadAnimations() {
        // Declare the in and out animations and initialize them
        val `in` = AnimationUtils.loadAnimation(
            this,
            android.R.anim.fade_in
        )
        val out = AnimationUtils.loadAnimation(
            this,
            android.R.anim.fade_out
        )

        // set the animation type of textSwitcher
        quoteTextSwitcher!!.inAnimation = `in`
        quoteTextSwitcher!!.outAnimation = out
        quoteTextSwitcher!!.animateFirstView = false
    }

    /***
     * Returns respected fragment that currentUser
     * selected from navigation menu
     */
    private fun loadCurrentFragment() {
        // set selected nav item to checked.
        setNavItemChecked()

        // if currentUser select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (supportFragmentManager.findFragmentByTag(CURRENT_TAG) != null) {
            drawer!!.closeDrawers()
            return
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        val mPendingRunnable = Runnable { // update the main content by replacing fragments
            val fragment = currentFragment
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            fragmentTransaction.replace(R.id.main_fragment_container, fragment, CURRENT_TAG)
            fragmentTransaction.commitAllowingStateLoss()
        }

        Handler(Looper.getMainLooper()).post(mPendingRunnable)

        // Closing drawer on item click
        drawer!!.closeDrawers()
    }// Contacts// Archive

    // Notes
    private val currentFragment: Fragment
        get() = when (navItemIndex) {
            0 -> {
                // Notes
                val notesFragment = NotesFragment()
                instance!!.currentFragment = notesFragment
                notesFragment
            }
            1 -> {
                // Archive
                val archivedNotesFragment = ArchivedNotesFragment()
                instance!!.currentFragment = archivedNotesFragment
                archivedNotesFragment
            }
            2 -> {
                // Contacts
                val contactsFragment = ContactsFragment()
                instance!!.currentFragment = contactsFragment
                contactsFragment
            }
            else -> {
                val defaultFragment = NotesFragment()
                instance!!.currentFragment = defaultFragment
                defaultFragment
            }
        }

    private fun setNavItemChecked() {
        navigationView!!.menu.getItem(navItemIndex).isChecked = true
    }

    /**
     * Called when a currentUser clicks on the navigation option "Sign out" and shows a dialog window to
     * make sure that the currentUser really wants to sign out.
     */
    private fun startSignOut() {
        val alertDialogBuilder = AlertDialog.Builder(this).setTitle(
            resources.getString(R.string.dialog_sign_out_title)
        )
            .setMessage(resources.getString(R.string.dialog_sign_out_msg))
            .setPositiveButton("Yes") { _, _ -> signOutUser() }
            .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = alertDialogBuilder.create()

        // Show AnonymousDialog if current user is Anonymous
        // else, show ordinary SignOutDialog
        if (currentUser!!.isAnonymous) {
            showAnonymousSignOutDialog()
        } else {
            dialog.show()
        }
    }

    private fun signOutUser() {
        val providerData = currentUser!!.providerData
        for (ui in providerData) {
            if (ui.providerId == GoogleAuthProvider.PROVIDER_ID) {
                instance!!.saveState()
                signOutGoogleUser()
            }
        }
    }

    private fun showAnonymousSignOutDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this).setTitle(
            resources.getString(R.string.dialog_anonymous_sign_out_title)
        )
            .setMessage(resources.getString(R.string.dialog_anonymous_sign_out_msg))
            .setPositiveButton("Yes") { _, _ -> signOutAnonymousUser() }
            .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    private fun signOutAnonymousUser() {
        // TODO: Extract to UseCase and call from ViewModel instead
        instance!!.authRepository.signOutAnonymousAccount(object : AuthCallback {
            override fun onSuccess() {
                showSignInActivity()
            }

            override fun onFailure(errorMessage: String?) {
                Log.e(TAG, "Failed to sign out Anonymous user: $errorMessage")
            }
        })
    }

    private fun signOutGoogleUser() {
        Log.d(TAG, "SignOutGoogleUser clicked: " + currentUser!!.email)
        // TODO: Extract to UseCase and call from ViewModel instead
        instance!!.authRepository.signOutGoogleAccount(object : AuthCallback {
            override fun onSuccess() {
                showSignInActivity()
            }

            override fun onFailure(errorMessage: String?) {
                Log.e(TAG, "Failed to sign out Google Account: $errorMessage")
            }
        })
    }

    private fun showSignInActivity() {
        val i = Intent(applicationContext, SignInActivity::class.java)
        startActivity(i)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finishAfterTransition()
    }

    override fun onBackPressed() {
        handleBackNavigation()
    }

    private fun handleBackNavigation() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawers()
            return
        }

        if (shouldLoadHomeFragOnBackPress) {
            if (navItemIndex != 0) {
                navItemIndex = 0
                CURRENT_TAG = TAG_NOTES_FRAGMENT
                loadCurrentFragment()
                return
            }

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }

            doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT)
                .show()
            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        }
    }

    override fun onStop() {
        super.onStop()
        if (instance!!.authService.currentUser != null) {
            instance!!.saveState()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance!!.authService.currentUser != null) {
            instance!!.saveState()
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        const val TAG_NOTES_FRAGMENT = "NOTES_FRAGMENT"
        const val TAG_ARCHIVED_NOTES_FRAGMENT = "ARCHIVED_NOTES_FRAGMENT"
        const val TAG_CONTACTS_FRAGMENT = "CONTACTS_FRAGMENT"
        var CURRENT_TAG = TAG_NOTES_FRAGMENT

        // index to identify current nav menu item
        var navItemIndex = 0
    }
}