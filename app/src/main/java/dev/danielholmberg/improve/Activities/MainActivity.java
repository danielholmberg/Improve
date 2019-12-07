package dev.danielholmberg.improve.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import dev.danielholmberg.improve.Adapters.ArchivedNotesAdapter;
import dev.danielholmberg.improve.Adapters.CompanyRecyclerViewAdapter;
import dev.danielholmberg.improve.Adapters.NotesAdapter;
import dev.danielholmberg.improve.Adapters.TagsAdapter;
import dev.danielholmberg.improve.Adapters.VipImagesAdapter;
import dev.danielholmberg.improve.Callbacks.FirebaseAuthCallback;
import dev.danielholmberg.improve.Fragments.ArchivedNotesFragment;
import dev.danielholmberg.improve.Fragments.ContactsFragment;
import dev.danielholmberg.improve.Fragments.NotesFragment;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Services.DriveServiceHelper;
import dev.danielholmberg.improve.Utilities.CircleTransform;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String TAG_NOTES_FRAGMENT = "NOTES_FRAGMENT";
    public static final String TAG_ARCHIVED_NOTES_FRAGMENT = "ARCHIVED_NOTES_FRAGMENT";
    public static final String TAG_CONTACTS_FRAGMENT = "CONTACTS_FRAGMENT";
    public static String CURRENT_TAG = TAG_NOTES_FRAGMENT;

    private Improve app;
    private DriveServiceHelper mDriveServiceHelper;

    // index to identify current nav menu item
    public static int navItemIndex = 0;
    // flag to load home fragment when currentUser presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private boolean doubleBackToExitPressedOnce = false;

    private Handler mHandler;

    private FirebaseUser currentUser;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private boolean hasloadedNavView = false;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private String currentQuote;
    private TextSwitcher quoteTextSwitcher;
    private int quoteUpdateTime = 20000;
    private SoundPool soundPool;
    private int quoteSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = Improve.getInstance();
        app.setMainActivityRef(this);
        currentUser = app.getAuthManager().getCurrentUser();

        // Retrieve VIP_USERS from Firebase RemoteConfig
        app.getFirebaseRemoteConfig().fetchAndActivate()
                .addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);
                        }

                        String retrievedVIPUsers = app.getFirebaseRemoteConfig().getString("vip_users");
                        Log.d(TAG, "Retrieved VIP_USERS: " + retrievedVIPUsers);

                        if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                            Log.d(TAG, "Current user: " + currentUser.getEmail());

                            app.setIsVIPUser(retrievedVIPUsers.contains(currentUser.getEmail()));
                        } else {
                            app.setIsVIPUser(false);
                        }

                        initNavDrawer();
                    }
                });


        initActivity();
        initData();

        loadCurrentFragment();
    }

    private void initActivity() {
        // Initializing the Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Initalizing NavigationDrawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Initializing the Handler for fragment transactions.
        mHandler = new Handler();

        initNavDrawer();
        initDriveService();
    }

    private void initDriveService() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if(account != null) {
            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(
                            this, Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(account.getAccount());
            com.google.api.services.drive.Drive googleDriveService =
                    new com.google.api.services.drive.Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName(getResources().getString(R.string.app_name))
                            .build();
            mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
            app.setDriveServiceHelper(mDriveServiceHelper);
        } else {
            Log.e(TAG, "Failed to initialize DriveServiceHelper.");
        }
    }

    private void initData() {
        app.setTagsAdapter(new TagsAdapter());
        app.setNotesAdapter(new NotesAdapter());
        app.setArchivedNotesAdapter(new ArchivedNotesAdapter());
        app.setCompanyRecyclerViewAdapter(new CompanyRecyclerViewAdapter());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(hasloadedNavView) {
                quoteTextSwitcher.setVisibility(View.GONE);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            if(hasloadedNavView) {
                quoteTextSwitcher.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initNavDrawer() {
        // Setting the Image, Name and Email in the NavigationDrawer Header.
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        RelativeLayout drawer_header_image_layout = (RelativeLayout) navigationView.getHeaderView(0).findViewById(R.id.drawer_header_image_layout);
        ImageView vip_user_symbol = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.vip_user_symbol);
        ImageView drawer_header_image = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_header_image_iv);
        TextView drawer_header_name = (TextView)  navigationView.getHeaderView(0).findViewById(R.id.drawer_header_name_tv);
        TextView drawer_header_email = (TextView)  navigationView.getHeaderView(0).findViewById(R.id.drawer_header_email_tv);

        if(app.isVIPUser()) {
            vip_user_symbol.setVisibility(View.VISIBLE);
        } else {
            vip_user_symbol.setVisibility(View.GONE);
        }

        if(currentUser.isAnonymous()) {
            drawer_header_image_layout.setVisibility(View.GONE);
            drawer_header_email.setVisibility(View.GONE);
            drawer_header_name.setVisibility(View.GONE);
        } else {

            Uri photoUri = currentUser.getPhotoUrl();

            List<? extends UserInfo> providerData = currentUser.getProviderData();
            for (UserInfo ui : providerData) {
                if(ui.getProviderId().equals(GoogleAuthProvider.PROVIDER_ID)) {
                    photoUri = ui.getPhotoUrl();
                    break;
                }
            }

            Picasso.get()
                    .load(photoUri)
                    .error(R.drawable.ic_error_no_photo_white)
                    .transform(new CircleTransform())
                    .resize(200, 200)
                    .centerCrop()
                    .into(drawer_header_image);
            drawer_header_name.setText(currentUser.getDisplayName());
            drawer_header_email.setText(currentUser.getEmail());
        }

        // Initializing navigation menu
        setUpNavigationView();
        setUpQuoteView();

        hasloadedNavView = true;
    }

    private void setUpNavigationView() {
        final Context context = this;
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                // Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    // Replacing the main content with correct fragment.
                    case R.id.nav_notes:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_NOTES_FRAGMENT;
                        loadCurrentFragment();
                        break;
                    case R.id.nav_archived_notes:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_ARCHIVED_NOTES_FRAGMENT;
                        loadCurrentFragment();
                        break;
                    case R.id.nav_contacts:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_CONTACTS_FRAGMENT;
                        loadCurrentFragment();
                        break;
                    case R.id.nav_feedback:
                        menuItem.setChecked(true);
                        drawer.closeDrawers();
                        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                            @Override
                            public void onDrawerClosed(View drawerView) {
                                super.onDrawerClosed(drawerView);
                                startActivity(new Intent(context, SubmitFeedbackActivity.class));
                                drawer.removeDrawerListener(this);
                            }
                        });
                        break;
                    case R.id.nav_privacy_policy:
                        menuItem.setChecked(true);
                        drawer.closeDrawers();
                        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                            @Override
                            public void onDrawerClosed(View drawerView) {
                                super.onDrawerClosed(drawerView);
                                startActivity(new Intent(context, PrivacyPolicyActivity.class));
                                drawer.removeDrawerListener(this);
                            }
                        });
                        break;
                    case R.id.nav_sign_out:
                        startSignOut();
                        drawer.closeDrawers();
                        return true;
                    default:
                        navItemIndex = 0;
                }

                return true;
            }
        });

        // Calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    private void setUpQuoteView() {
        quoteTextSwitcher = (TextSwitcher) navigationView.findViewById(R.id.nav_view_bottom_quote);

        loadAnimations();
        loadQuoteSound();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setQuoteText();
                            }
                        });
                    }
                }, 0, quoteUpdateTime);

        quoteTextSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setQuoteText();
                soundPool.play(quoteSound, 1, 1, 0, 0, 1);
            }
        });
    }

    private void loadQuoteSound() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();
        quoteSound = soundPool.load(this, R.raw.quote_easter_egg, 1);
    }

    private void setQuoteText() {
        String[] quotesArray = getResources().getStringArray(R.array.quotes);
        currentQuote = quotesArray[new Random().nextInt(quotesArray.length)];

        if(currentQuote == null || currentQuote.isEmpty()) {
            currentQuote = "There is always room for improvement.";
        }

        quoteTextSwitcher.setText(currentQuote);
    }

    private void loadAnimations() {
        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out);

        // set the animation type of textSwitcher
        quoteTextSwitcher.setInAnimation(in);
        quoteTextSwitcher.setOutAnimation(out);
        quoteTextSwitcher.setAnimateFirstView(false);
    }

    /***
     * Returns respected fragment that currentUser
     * selected from navigation menu
     */
    private void loadCurrentFragment() {
        // set selected nav item to checked.
        setNavItemChecked();

        // if currentUser select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getCurrentFragment();

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.main_fragment_container, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        // Closing drawer on item click
        drawer.closeDrawers();
    }

    private Fragment getCurrentFragment() {
        switch (navItemIndex) {
            case 0:
                // Notes
                NotesFragment notesFragment = new NotesFragment();
                app.setCurrentFragment(notesFragment);
                return notesFragment;
            case 1:
                // Archive
                ArchivedNotesFragment archivedNotesFragment = new ArchivedNotesFragment();
                app.setCurrentFragment(archivedNotesFragment);
                return archivedNotesFragment;
            case 2:
                // Contacts
                ContactsFragment contactsFragment = new ContactsFragment();
                app.setCurrentFragment(contactsFragment);
                return contactsFragment;
            default:
                NotesFragment defaultFragment = new NotesFragment();
                app.setCurrentFragment(defaultFragment);
                return defaultFragment;
        }
    }

    private void setNavItemChecked() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    /**
     * Called when a currentUser clicks on the navigation option "Sign out" and shows a dialog window to
     * make sure that the currentUser really wants to sign out.
     */
    private void startSignOut() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.dialog_sign_out_title))
                        .setMessage(getResources().getString(R.string.dialog_sign_out_msg))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                signOutUser();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
        final AlertDialog dialog = alertDialogBuilder.create();

        // Show AnonymousDialog if current user is Anonymous
        // else, show ordinary SignOutDialog
        if(currentUser.isAnonymous()) {
            showAnonymousSignOutDialog();
        } else {
            dialog.show();
        }
    }

    private void signOutUser() {
        List<? extends UserInfo> providerData = currentUser.getProviderData();
        for (UserInfo ui : providerData) {
            if(ui.getProviderId().equals(GoogleAuthProvider.PROVIDER_ID)) {
                app.saveState();
                signOutGoogleUser();
            }
        }
    }

    private void showAnonymousSignOutDialog() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.dialog_anonymous_sign_out_title))
                        .setMessage(getResources().getString(R.string.dialog_anonymous_sign_out_msg))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                signOutAnonymousUser();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    private void signOutAnonymousUser() {
        app.getAuthManager().signOutAnonymousAccount(new FirebaseAuthCallback() {
            @Override
            public void onSuccess() {
                showSignInActivity();
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Failed to sign out Anonymous user: " + errorMessage);
            }
        });
    }

    private void signOutGoogleUser() {
        Log.d(TAG, "SignOutGooleUser clicked: " + currentUser.getEmail());
        app.getAuthManager().signOutGoogleAccount(new FirebaseAuthCallback() {
            @Override
            public void onSuccess() {
                showSignInActivity();
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "Failed to sign out Google Account: " + errorMessage);
            }
        });
    }

    private void showSignInActivity() {
        Intent i = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finishAfterTransition();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        if (shouldLoadHomeFragOnBackPress) {
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_NOTES_FRAGMENT;
                loadCurrentFragment();
            } else {

                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(app.getAuthManager().getCurrentUser() != null) {
            app.saveState();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(app.getAuthManager().getCurrentUser() != null) {
            app.saveState();
        }
    }
}
