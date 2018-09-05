package dev.danielholmberg.improve.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import dev.danielholmberg.improve.Callbacks.FirebaseAuthCallback;
import dev.danielholmberg.improve.Fragments.ArchivedNotesFragment;
import dev.danielholmberg.improve.Fragments.ContactsFragment;
import dev.danielholmberg.improve.Fragments.NotesFragment;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.CircleTransform;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String TAG_NOTES_FRAGMENT = "NOTES_FRAGMENT";
    public static final String TAG_ARCHIVED_NOTES_FRAGMENT = "ARCHIVED_NOTES_FRAGMENT";
    public static final String TAG_CONTACTS_FRAGMENT = "CONTACTS_FRAGMENT";
    public static String CURRENT_TAG = TAG_NOTES_FRAGMENT;

    private Improve app;
    private Context context;

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
        context = getApplicationContext();

        // Initializing the Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Getting current signed in User reference.
        currentUser = app.getAuthManager().getCurrentUser();

        // Initalizing NavigationDrawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Initializing the Handler for fragment transactions.
        mHandler = new Handler();

        initNavDrawer();

        loadCurrentFragment();
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
        ImageView drawer_header_image = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_header_image_iv);
        TextView drawer_header_name = (TextView)  navigationView.getHeaderView(0).findViewById(R.id.drawer_header_name_tv);
        TextView drawer_header_email = (TextView)  navigationView.getHeaderView(0).findViewById(R.id.drawer_header_email_tv);

        if(currentUser.isAnonymous()) {
            drawer_header_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_anonymous_outline_white));
            drawer_header_email.setVisibility(View.GONE);
            drawer_header_name.setVisibility(View.GONE);
        } else {
            Picasso.with(this)
                    .load(currentUser.getPhotoUrl())
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
        // selecting appropriate nav menu item
        selectNavMenu();

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
                return new NotesFragment();
            case 1:
                // Archive
                return new ArchivedNotesFragment();
            case 2:
                // Contacts
                return new ContactsFragment();
            default:
                return new NotesFragment();
        }
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
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
        if(currentUser.getProviders() != null && currentUser.getProviders().size() > 0) {
            // The user has authenticated with some provider, eg. Google
            String providerId = currentUser.getProviders().get(0);
            switch (providerId) {
                case GoogleAuthProvider.PROVIDER_ID:
                    signOutGoogleUser();
                    break;
                default:
                    break;
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
            public void onFailure(String errorMessage) {}
        });
    }

    private void signOutGoogleUser() {
        app.getAuthManager().signOutGoogleAccount(new FirebaseAuthCallback() {
            @Override
            public void onSuccess() {
                showSignInActivity();
            }

            @Override
            public void onFailure(String errorMessage) {}
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
}
