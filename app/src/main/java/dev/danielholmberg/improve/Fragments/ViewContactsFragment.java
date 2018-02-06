package dev.danielholmberg.improve.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
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
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Adapters.ContactsRecyclerViewAdapter;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-18.
 */

public class ViewContactsFragment extends Fragment implements SearchView.OnQueryTextListener{
    private static final String TAG = "ViewContactsFragment";
    private static final String INTERNAL_STORAGE_KEY = "contacts";
    private static final String filename = "contacts";

    private FirebaseFirestore firestoreDB;
    private RecyclerView contactsRecyclerView;
    private List<Contact> contactList = new ArrayList<>();
    private List<Contact> cachedContacts;
    public ContactsRecyclerViewAdapter recyclerViewAdapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fab;

    public ViewContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_contacts,
                container, false);

        // Initialize Firestore Database.
        firestoreDB = FirebaseFirestore.getInstance();

        // Initialize View components to be used.
        contactsRecyclerView = (RecyclerView) view.findViewById(R.id.contacts_list);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh_contacts);
        fab = (FloatingActionButton) getActivity().getWindow().findViewById(R.id.add_contact);

        LinearLayoutManager recyclerLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        contactsRecyclerView.setLayoutManager(recyclerLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(contactsRecyclerView.getContext(),
                        recyclerLayoutManager.getOrientation());
        contactsRecyclerView.addItemDecoration(dividerItemDecoration);

        // Initialize the adapter for RecycleView.
        initAdapter();

        // Add a RefreshListener to retrieve the newest instance of the Firestore Database.
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "Refreshing OnMyMinds");
                getDataFromFirestore();
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

        return view;
    }

    /**
     * Initializes a custom RecycleViewAdapter.
     */
    private void initAdapter() {
        recyclerViewAdapter = new
                ContactsRecyclerViewAdapter(contactList,
                getActivity(), firestoreDB);
        contactsRecyclerView.setAdapter(recyclerViewAdapter);

        try {
            // Try to read the already stored list of contacts in Internal Storage.
            cachedContacts = (List<Contact>) InternalStorage.readObject(getContext(), INTERNAL_STORAGE_KEY);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(cachedContacts != null) {
            // The Internal Storage list has been initialized in a previous session.
            if(cachedContacts.isEmpty()) {
                // The list of already stored list of contacts is empty.
                // Get the list from the Firestore Database instead.
                Log.d(TAG, "Getting contacts from Firestore Database");
                getDataFromFirestore();
            } else {
                // The list of already stored list of contacts is NOT empty.
                // Add the stored list of contacts to the custom RecycleViewAdapter.
                Log.d(TAG, "Getting contacts from Internal Storge");
                recyclerViewAdapter.setAdapterList(cachedContacts);
                recyclerViewAdapter.notifyDataSetChanged();
                contactsRecyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            // The Internal Storage list has NOT been initialized in a previous session.
            // Get the list from the Firestore Database instead.
            Log.d(TAG, "Getting contacts from Firestore Database");
            getDataFromFirestore();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Filter the contact list.
        recyclerViewAdapter.filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Filter the contact list.
        recyclerViewAdapter.filter(newText);
        return false;
    }

    /**
     * Retrieves contact list data from Firestore Database
     */
    private void getDataFromFirestore() {
        Log.d(TAG, "getDataFromFirestore()");
        contactsRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        // Run the Query on a new thread for performance purposes.
        new Thread(new Runnable() {
            @Override
            public void run() {
                firestoreDB.collection("companies")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Empty the list so that it can be refilled.
                                    contactList.clear();

                                    if (task.getResult().isEmpty()) {
                                        // No companies exists.
                                        Log.e(TAG, "No companies exists.");
                                    } else {
                                        // Get data from each stored company.
                                        for (final DocumentSnapshot company : task.getResult()) {
                                            firestoreDB.collection("companies")
                                                    .document(company.getId())
                                                    .collection("contacts")
                                                    .orderBy("firstName")
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                if (task.getResult().isEmpty()) {
                                                                    Log.e(TAG, "No contacts exists for company: " + company.getId());
                                                                    firestoreDB.collection("companies")
                                                                            .document(company.getId()).delete();
                                                                } else {
                                                                    // Getting all contacts from {company} and adding them to the contactList.
                                                                    for (DocumentSnapshot contact : task.getResult()) {
                                                                        Log.d(TAG, "Getting data from contact: " + contact.getId());
                                                                        Contact c = contact.toObject(Contact.class);
                                                                        c.setCID(contact.getId());
                                                                        contactList.add(c);
                                                                    }
                                                                    try {
                                                                        // Writing all the retrieved contacts to Internal Storage for offline use.
                                                                        InternalStorage.writeObject(getContext(), INTERNAL_STORAGE_KEY, contactList);
                                                                    } catch (IOException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    Log.d(TAG, "Wrote contacts to Internal Storage");
                                                                    recyclerViewAdapter.setAdapterList(contactList);
                                                                    recyclerViewAdapter.notifyDataSetChanged();
                                                                    contactsRecyclerView.setVisibility(View.VISIBLE);
                                                                }
                                                                Log.d(TAG,"Done getting data from company: " + company.getId());
                                                            }  else {
                                                                // Error occured when retrieving contacts for {company}.
                                                                Log.e(TAG, "Error getting contacts for company " + company.getId() + ":", task.getException());
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                } else {
                                    // Error occured when retrieving companies from Firestore Database.
                                    Log.e(TAG, "Error getting companies: ", task.getException());
                                }
                                // Done getting all the contacts stored in Firestore Database.
                                Log.d(TAG, "Done getting Data");
                                progressBar.setVisibility(View.GONE);
                                contactsRecyclerView.setVisibility(View.VISIBLE);
                            }
                        });
            }
        }).start();
    }
}
