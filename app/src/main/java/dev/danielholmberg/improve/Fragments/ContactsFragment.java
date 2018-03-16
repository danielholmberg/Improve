package dev.danielholmberg.improve.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Activities.AddContactActivity;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.ViewHolders.ContactViewHolder;

/**
 * Created by DanielHolmberg on 2018-01-18.
 */

public class ContactsFragment extends Fragment implements SearchView.OnQueryTextListener{
    private static final String TAG = "ContactsFragment";

    private Improve app;
    private FirebaseStorageManager storageManager;

    private View view;
    private RecyclerView contactsRecyclerView;
    private List<Contact> contactList = new ArrayList<>();
    private List<Contact> cachedContacts;
    private TextView emptyListText;
    private FirebaseRecyclerAdapter recyclerAdapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fab;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();
        // Enable the OptionsMenu to show the SearchView.
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contacts,
                container, false);

        // Initialize View components to be used.
        contactsRecyclerView = (RecyclerView) view.findViewById(R.id.contacts_list);
        emptyListText = (TextView) view.findViewById(R.id.empty_contact_list_tv);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh_contacts);
        fab = (FloatingActionButton) view.findViewById(R.id.add_contact);

        // Initialize the LinearLayoutManager
        LinearLayoutManager recyclerLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        contactsRecyclerView.setLayoutManager(recyclerLayoutManager);

        // Initialize the adapter for RecycleView.
        initAdapter();
        contactsRecyclerView.setAdapter(recyclerAdapter);

        // Add a RefreshListener to retrieve the newest instance of the Firestore Database.
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "--- Refreshing OnMyMinds...");
                recyclerAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Add a OnScrollListener to change when to show the Floating Action Button for adding a new OnMyMind.
        contactsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy>0 && fab.isShown())
                    // Hide the FAB when the user scrolls down.
                    fab.hide();
                if(dy<0 && !fab.isShown())
                    // Show the FAB when the user scrolls up.
                    fab.show();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle action add a new Contact.
                addContact();
            }
        });

        return view;
    }

    private void initAdapter() {
        Query query = storageManager.getContactsRef();

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(query, Contact.class)
                        .build();

        recyclerAdapter = new FirebaseRecyclerAdapter<Contact, ContactViewHolder>(options) {
            @Override
            public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_contact, parent, false);

                return new ContactViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ContactViewHolder holder, int position, Contact model) {
                holder.bindModelToView(model);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        recyclerAdapter.stopListening();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_contacts_toolbar_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setHint(R.string.search_hint);
        searchEditText.setTextColor(getResources().getColor(R.color.titleTextColor));
        searchEditText.setHintTextColor(getResources().getColor(R.color.searchHintTextColor));
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Filter the contact list.
        //filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Filter the contact list.
        //filter(newText);
        return false;
    }

    /**
     * Called when a user clicks on the Floating Action Button to add a new Contact.
     */
    public void addContact() {
        Intent addContactIntent = new Intent(getContext(), AddContactActivity.class);
        startActivity(addContactIntent);
    }

    /*
    public void filter(String text) {
        if(contactsList != null && contactsListCopy != null) {
            contactsList.clear();
            if (text.isEmpty()) {
                contactsList.addAll(contactsListCopy);
            } else {
                text = text.toLowerCase();
                for (Contact contact : contactsListCopy) {
                    if (contact.getName().toLowerCase().contains(text) || contact.getCompany().toLowerCase().contains(text)) {
                        contactsList.add(contact);
                    }
                }
            }
            recyclerAdapter.notifyDataSetChanged();
        }
    }*/
}
