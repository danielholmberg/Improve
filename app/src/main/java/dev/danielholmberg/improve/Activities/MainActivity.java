package dev.danielholmberg.improve.Activities;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import dev.danielholmberg.improve.Fragments.ContactsFragment;
import dev.danielholmberg.improve.Fragments.NotesFragment;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.Utilities.CircleTransform;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_NOTES_FRAGMENT = "NOTES_FRAGMENT";
    private static final String TAG_CONTACTS_FRAGMENT = "CONTACTS_FRAGMENT";
    private static String CURRENT_TAG = TAG_NOTES_FRAGMENT;

    private Improve app;

    // index to identify current nav menu item
    public static int navItemIndex = 0;
    private static final String[] subTitles = {
            "Notes",
            "Contacts",
    };
    // flag to load home fragment when currentUser presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private int backPressCounter = 0;

    private Handler mHandler;

    private FirebaseAuth fireAuth;
    private FirebaseUser currentUser;
    private GoogleSignInClient mGoogleSignInClient;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = Improve.getInstance();

        // Initializing the Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Getting current signed in User reference.
        currentUser = app.getAuthManager().getCurrentUser();
        mGoogleSignInClient = app.getAuthManager().getmGoogleSignInClient();

        // Initalizing NavigationDrawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // Initializing the Handler for fragment transactions.
        mHandler = new Handler();

        // Setting the Image, Name and Email in the NavigationDrawer Header.
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        ImageView drawer_header_image = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_header_image_iv);
        TextView drawer_header_name = (TextView)  navigationView.getHeaderView(0).findViewById(R.id.drawer_header_name_tv);
        TextView drawer_header_email = (TextView)  navigationView.getHeaderView(0).findViewById(R.id.drawer_header_email_tv);
        Picasso.with(this)
                .load(currentUser.getPhotoUrl())
                .error(R.drawable.ic_error_no_photo_white)
                .transform(new CircleTransform())
                .resize(200, 200)
                .centerCrop()
                .into(drawer_header_image);
        drawer_header_name.setText(currentUser.getDisplayName());
        drawer_header_email.setText(currentUser.getEmail());

        // Initializing navigation menu
        setUpNavigationView();

        loadCurrentFragment();
    }

    /***
     * Returns respected fragment that currentUser
     * selected from navigation menu
     */
    private void loadCurrentFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

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
                // OnMyMinds
                return new NotesFragment();
            case 1:
                // Contacts
                return new ContactsFragment();
            default:
                return new NotesFragment();
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

                // Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    // Replacing the main content with correct fragment.
                    case R.id.nav_notes:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_NOTES_FRAGMENT;
                        break;
                    case R.id.nav_contacts:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_CONTACTS_FRAGMENT;
                        break;
                    case R.id.nav_sign_out:
                        startSignOut();
                        drawer.closeDrawers();
                        return true;
                    default:
                        navItemIndex = 0;
                }

                loadCurrentFragment();

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
                new AlertDialog.Builder(this).setTitle("Sign out")
                        .setMessage("Do you really want to sign out?")
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
        // Disconnects the Google account.
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        app.getAuthManager().getFireAuth().signOut();

                        Log.d(TAG, "*** User successfully Signed Out ***");
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                });

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when currentUser is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if currentUser is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_NOTES_FRAGMENT;
                loadCurrentFragment();
            } else {
                backPressCounter++;
                Toast exitToast = Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT);
                exitToast.show();
                if(backPressCounter >= 2) {
                    exitToast.cancel();
                    backPressCounter = 0;
                    finish();
                }
            }
        }
    }
}
