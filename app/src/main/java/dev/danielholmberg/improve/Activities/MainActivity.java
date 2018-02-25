package dev.danielholmberg.improve.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import dev.danielholmberg.improve.CircleTransform;
import dev.danielholmberg.improve.Fragments.ContactsFragment;
import dev.danielholmberg.improve.Fragments.OnMyMindFragment;
import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_ONMYMINDS_FRAGMENT = "ONMYMINDS_FRAGMENT";
    private static final String TAG_CONTACTS_FRAGMENT = "CONTACTS_FRAGMENT";
    private static final String TAG_ARCHIVE_FRAGMENT = "ARCHIVE_FRAGMENT";
    private static String CURRENT_TAG = TAG_ONMYMINDS_FRAGMENT;

    // index to identify current nav menu item
    public static int navItemIndex = 0;
    private static final String[] subTitles = {
            "OnMyMinds",
            "Contacts",
            "Archive",
            "Settings",
    };
    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;

    private FirebaseAuth mAuth;
    private GoogleSignInAccount user;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup Internal Storage
        try {
            InternalStorage.createStorage(this);
        } catch (IOException e) {
            Log.e(TAG, "Failed to setup Storage files: ");
            e.printStackTrace();
            Toast.makeText(this, "Failed to setup Storage files", Toast.LENGTH_SHORT).show();
        }

        // Setup the Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Get currentUser information
        mAuth = FirebaseAuth.getInstance();
        user = GoogleSignIn.getLastSignedInAccount(this);

        // Initalize NavigationDrawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        mHandler = new Handler();

        // Set the Image, Name and Email in the NavigationDrawer Header.
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        ImageView drawer_header_image = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_header_image_iv);
        TextView drawer_header_name = (TextView)  navigationView.getHeaderView(0).findViewById(R.id.drawer_header_name_tv);
        TextView drawer_header_email = (TextView)  navigationView.getHeaderView(0).findViewById(R.id.drawer_header_email_tv);
        Picasso.with(this)
                .load(user.getPhotoUrl())
                .error(R.drawable.ic_error_no_photo)
                .transform(new CircleTransform())
                .resize(200, 200)
                .centerCrop()
                .into(drawer_header_image);
        drawer_header_name.setText(user.getDisplayName());
        drawer_header_email.setText(user.getEmail());

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_ONMYMINDS_FRAGMENT;
            loadCurrentFragment();
        }

    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadCurrentFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
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

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getCurrentFragment() {
        switch (navItemIndex) {
            case 0:
                // OnMyMinds
                OnMyMindFragment ommFragment = new OnMyMindFragment();
                return ommFragment;
            case 1:
                // Contacts
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
            case 2:
                // Archive fragment
                return null;
            case 3:
                // Settings fragment
                return null;
            default:
                return new OnMyMindFragment();
        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setSubtitle(subTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_onmyminds:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_ONMYMINDS_FRAGMENT;
                        break;
                    case R.id.nav_contacts:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_CONTACTS_FRAGMENT;
                        break;
                    case R.id.nav_archive:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_ARCHIVE_FRAGMENT;
                        break;
                    case R.id.nav_settings:
                        // launch new intent instead of loading fragment
                        // TODO - startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        // drawer.closeDrawers();
                        return true;
                    case R.id.nav_sign_out:
                        startSignOut();
                        drawer.closeDrawers();
                        return true;
                    default:
                        navItemIndex = 0;
                }

                // Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadCurrentFragment();

                return true;
            }
        });

        // Calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    /**
     * Called when a user clicks on the navigation option "Sign out" and shows a dialog window to
     * make sure that the user really wants to sign out.
     */
    private void startSignOut() {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(this).setTitle("Sign out")
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
        dialog.show();
    }

    private void signOutUser() {
        mAuth.signOut();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_ONMYMINDS_FRAGMENT;
                loadCurrentFragment();
                return;
            } else {
                AlertDialog.Builder alertDialogBuilder =
                        new AlertDialog.Builder(this).setTitle("Exit application")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Terminate the application.
                                        finish();
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
        }
    }
}
