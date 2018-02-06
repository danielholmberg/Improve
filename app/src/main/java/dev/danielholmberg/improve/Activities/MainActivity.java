package dev.danielholmberg.improve.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;

import dev.danielholmberg.improve.Adapters.ViewPagerAdapter;
import dev.danielholmberg.improve.Fragments.ViewContactsFragment;
import dev.danielholmberg.improve.Fragments.ViewOnMyMindFragment;
import dev.danielholmberg.improve.R;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = MainActivity.class.getSimpleName();

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

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

    private void addOnMyMind() {
        Intent i = new Intent(this, AddOnMyMindActivity.class);
        startActivity(i);
    }

    public void addContact() {
        Intent i = new Intent(this, AddContactActivity.class);
        startActivity(i);
    }
}
