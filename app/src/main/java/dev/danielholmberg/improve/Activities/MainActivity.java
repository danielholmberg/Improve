package dev.danielholmberg.improve.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import dev.danielholmberg.improve.Adapters.ViewPagerAdapter;
import dev.danielholmberg.improve.CircleTransform;
import dev.danielholmberg.improve.Fragments.ViewContactsFragment;
import dev.danielholmberg.improve.Fragments.ViewOnMyMindFragment;
import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int FORM_REQUEST_CODE = 9995;

    private FirebaseAuth mAuth;
    private GoogleSignInAccount user;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.ic_tab_omms,
            R.drawable.ic_tab_contacts,
    };
    private ViewOnMyMindFragment tab1;
    private ViewContactsFragment tab2;

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

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        // Initalize NavigationDrawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set the Image, Name and Email in the NavigationDrawer Header.
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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

        final FloatingActionButton fab_add_contact = (FloatingActionButton) findViewById(R.id.add_contact);
        fab_add_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle action add a new Contact.
                addContact();
            }
        });

        final FloatingActionButton fab_add_omm = (FloatingActionButton) findViewById(R.id.add_omm);
        fab_add_omm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handel action add a new OnMyMind.
                addOnMyMind();
            }
        });


        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Fade in
                AlphaAnimation in = new AlphaAnimation(0.0f, 1.0f);
                in.setDuration(800);
                if (tab.getPosition() == 0) {
                    // OnMyMind Tab
                    fab_add_omm.startAnimation(in);
                    fab_add_omm.setVisibility(View.VISIBLE);
                } else if(tab.getPosition() == 1) {
                    // Contacts Tab
                    fab_add_contact.startAnimation(in);
                    fab_add_contact.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Fade out
                AlphaAnimation out = new AlphaAnimation(1.0f, 0.0f);
                out.setDuration(100);
                if (tab.getPosition() == 0) {
                    // OnMyMind Tab
                    fab_add_omm.startAnimation(out);
                    fab_add_omm.setVisibility(View.GONE);
                } else if(tab.getPosition() == 1) {
                    // Contacts Tab
                    fab_add_contact.startAnimation(out);
                    fab_add_contact.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        setupTabIcons();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        tab1 = new ViewOnMyMindFragment();
        tab2 = new ViewContactsFragment();
        adapter.addFragment(tab1, "OnMyMind");
        adapter.addFragment(tab2, "Contacts");
        viewPager.setAdapter(adapter);
    }

    /**
     * Called when a user clicks on the Floating Action Button to add a new OnMyMind.
     */
    private void addOnMyMind() {
        Intent i = new Intent(this, AddOnMyMindActivity.class);
        startActivityForResult(i, FORM_REQUEST_CODE);
    }

    /**
     * Called when a user clicks on the Floating Action Button to add a new Contact.
     */
    public void addContact() {
        Intent i = new Intent(this, AddContactActivity.class);
        startActivityForResult(i, FORM_REQUEST_CODE);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // TODO - Switch to Home fragment.
        } else if (id == R.id.nav_archive) {
            // TODO - Switch to Archive fragment.
        } else if (id == R.id.nav_search) {
            // TODO - Request focus of Search View in ContactTab.
        } else if (id == R.id.nav_sign_out) {
            startSignOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FORM_REQUEST_CODE:
                if (resultCode == AddOnMyMindActivity.OMM_ADDED) {
                    Snackbar.make(viewPager, "OnMyMind added successfully", Snackbar.LENGTH_SHORT).show();
                } else if (resultCode == AddOnMyMindActivity.OMM_UPDATED) {
                    Snackbar.make(viewPager, "OnMyMind updated successfully", Snackbar.LENGTH_SHORT).show();
                }
        }    }

    @Override
    public void onBackPressed() {
        if (tabLayout.getSelectedTabPosition() == 0) {
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
        } else if (tabLayout.getSelectedTabPosition() == 1) {
            tabLayout.getTabAt(0).select();
        } else {
            super.onBackPressed();
        }
    }
}
