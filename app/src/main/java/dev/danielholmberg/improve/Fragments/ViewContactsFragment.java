package dev.danielholmberg.improve.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.danielholmberg.improve.Adapters.ContactsRecyclerViewAdapter;
import dev.danielholmberg.improve.Components.Contact;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-18.
 */

public class ViewContactsFragment extends Fragment {
    private static final String TAG = "ViewContactsFragment";
    private FirebaseFirestore firestoreDB;
    private RecyclerView contactsRecyclerView;
    private ProgressBar progressBar;
    private TextView progressBarText;
    private Handler handler = new Handler();
    private List<Contact> contactList = new ArrayList<>();
    private ContactsRecyclerViewAdapter recyclerViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public ViewContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_contacts,
                container, false);

        firestoreDB = FirebaseFirestore.getInstance();

        contactsRecyclerView = (RecyclerView) view.findViewById(R.id.contacts_list);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        recyclerViewAdapter = new
                ContactsRecyclerViewAdapter(contactList,
                getActivity(), firestoreDB);
        contactsRecyclerView.setAdapter(recyclerViewAdapter);

        LinearLayoutManager recyclerLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        contactsRecyclerView.setLayoutManager(recyclerLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(contactsRecyclerView.getContext(),
                        recyclerLayoutManager.getOrientation());
        contactsRecyclerView.addItemDecoration(dividerItemDecoration);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh_contacts);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "Refreshing OnMyMinds");
                getDocumentsFromCollection();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        getDocumentsFromCollection();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void getDocumentsFromCollection() {
        contactsRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                firestoreDB.collection("companies")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    // New list
                                    contactList = new ArrayList<>();

                                    if (task.getResult().isEmpty()) {
                                        Log.e(TAG, "No companies exists.");
                                    } else {
                                        for (final DocumentSnapshot company : task.getResult()) {
                                            Log.d(TAG, "Getting data from company: " + company.getId());
                                            firestoreDB.collection("companies")
                                                    .document(company.getId())
                                                    .collection("contacts")
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                if (task.getResult().isEmpty()) {
                                                                    Log.e(TAG, "No contacts exists for company: " + company.getId());
                                                                } else {
                                                                    for (DocumentSnapshot contact : task.getResult()) {
                                                                        Log.d(TAG, "Getting data from contact: " + contact.getId());
                                                                        Contact c = contact.toObject(Contact.class);
                                                                        c.setCID(contact.getId());
                                                                        contactList.add(c);
                                                                        Log.d(TAG, "Full name: " + c.getFullName());
                                                                        Log.d(TAG, "Email: " + c.getEmail());
                                                                        Log.d(TAG, "Company: " + c.getCompany());
                                                                        Log.d(TAG, "ContactList: " + Arrays.toString(contactList.toArray()));
                                                                        Log.d(TAG, "Done getting contact: " + c.getCID());
                                                                    }
                                                                    recyclerViewAdapter.setAdapterList(contactList);
                                                                    recyclerViewAdapter.notifyDataSetChanged();
                                                                    contactsRecyclerView.setVisibility(View.VISIBLE);
                                                                }
                                                                Log.d(TAG,"Done getting data from company: " + company.getId());
                                                            }  else {
                                                                Log.e(TAG, "Error getting contacts for company " + company.getId() + ":", task.getException());
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "Error getting companies: ", task.getException());
                                }
                                Log.d(TAG, "Done getting Data");
                                progressBar.setVisibility(View.GONE);
                                contactsRecyclerView.setVisibility(View.VISIBLE);
                            }
                        });
            }
        }).start();
    }
}
