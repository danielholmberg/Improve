package dev.danielholmberg.improve.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Activities.AddOnMyMindActivity;
import dev.danielholmberg.improve.Adapters.OnMyMindsRecyclerViewAdapter;
import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-20.
 */

public class OnMyMindFragment extends Fragment {
    private static final String TAG = "OnMyMindFragment";
    private static final int FORM_REQUEST_CODE = 9995;

    private FirebaseFirestore firestoreDB;
    private String userId;
    private View view;
    private RecyclerView ommsRecyclerView;
    private List<OnMyMind> onmymindList = new ArrayList<>();
    private List<OnMyMind> cachedOnMyMinds;
    private OnMyMindsRecyclerViewAdapter recyclerViewAdapter;
    private TextView emptyListText;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fab;

    public OnMyMindFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_omms,
                container, false);

        // Initialize Firestore Database.
        firestoreDB = FirebaseFirestore.getInstance();

        // Get current userId.
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize View components to be used.
        ommsRecyclerView = (RecyclerView) view.findViewById(R.id.omms_list);
        emptyListText = (TextView) view.findViewById(R.id.empty_omms_list_tv);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh_omms);
        fab = (FloatingActionButton) view.findViewById(R.id.add_omm);

        LinearLayoutManager recyclerLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        ommsRecyclerView.setLayoutManager(recyclerLayoutManager);

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

        // Add a OnScrollListener to change when to show the Floating Action Button for adding
        // a new OnMyMind.
        ommsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
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
                // Handel action add a new OnMyMind.
                addOnMyMind();
            }
        });

        return view;
    }

    /**
     * Initializes a custom RecycleViewAdapter.
     */
    private void initAdapter() {
        recyclerViewAdapter = new
                OnMyMindsRecyclerViewAdapter(onmymindList,
                getActivity(), firestoreDB);
        ommsRecyclerView.setAdapter(recyclerViewAdapter);

        try {
            // Try to read the already stored list of OnMyMinds in Internal Storage.
            cachedOnMyMinds = (ArrayList<OnMyMind>) InternalStorage.readObject(InternalStorage.onmyminds);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read from Internal Storage: ");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(cachedOnMyMinds != null) {
            // The Internal Storage list has been initialized in a previous session.
            if(cachedOnMyMinds.isEmpty()) {
                // The already stored list of OnMyMinds is empty.
                // Get the list from the Firestore Database instead.
                getDataFromFirestore();
            } else {
                // The list of already stored list of OnMyMinds is NOT empty.
                // Add the stored list of OnMyMinds to the custom RecycleViewAdapter.
                Log.d(TAG, "*** Using data from Internal Storage ***");
                recyclerViewAdapter.setAdapterList(cachedOnMyMinds);
                recyclerViewAdapter.notifyDataSetChanged();
                ommsRecyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            // The Internal Storage list has NOT been initialized in a previous session.
            // Get the list from the Firestore Database instead.
            getDataFromFirestore();
        }
    }

    /**
     * Called when a user clicks on the Floating Action Button to add a new OnMyMind.
     */
    private void addOnMyMind() {
        Intent addOnMyMindIntent = new Intent(getContext(), AddOnMyMindActivity.class);
        startActivity(addOnMyMindIntent);
    }

    /**
     * Retrieves OnMyMind stored in Firestore Database.
     */
    private void getDataFromFirestore() {
        Log.d(TAG, "Getting data from Firestore...");
        ommsRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        // Run the Query on a new thread for performance purposes.
        new Thread(new Runnable() {
            @Override
            public void run() {
                firestoreDB.collection("users")
                        .document(userId)
                        .collection("onmyminds")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Empty the list so that it can be refilled.
                                    onmymindList.clear();

                                    if(task.getResult().isEmpty()){
                                        // No OnMyMinds exists.
                                        Log.e(TAG, "No OnMyMinds exists");
                                        try {
                                            // Writing a empty list to the Internal Storage.
                                            InternalStorage.writeObject(InternalStorage.onmyminds, onmymindList);
                                        } catch (IOException e) {
                                            Log.e(TAG, "Failed to write an empty OnMyMinds list to Internal Storage file: ");
                                            e.printStackTrace();
                                        }
                                        recyclerViewAdapter.setAdapterList(onmymindList);
                                        recyclerViewAdapter.notifyDataSetChanged();
                                        emptyListText.setVisibility(View.VISIBLE);
                                    } else {
                                        // Getting data from each stored OnMyMind.
                                        // and adding it to the list.
                                        for (DocumentSnapshot onmymind : task.getResult()) {
                                            OnMyMind omm = onmymind.toObject(OnMyMind.class);
                                            omm.setId(onmymind.getId());
                                            onmymindList.add(omm);
                                        }
                                        try {
                                            // Writing all the retrieved OnMyMinds to Internal Storage for offline use.
                                            InternalStorage.writeObject(InternalStorage.onmyminds, onmymindList);
                                            Log.d(TAG, "Wrote OnMyMinds to Internal Storage");
                                        } catch (IOException e) {
                                            Log.e(TAG, "Failed to write data from Firestore to Internal Storage file: ");
                                            e.printStackTrace();
                                        }
                                        recyclerViewAdapter.setAdapterList(onmymindList);
                                        recyclerViewAdapter.notifyDataSetChanged();
                                        emptyListText.setVisibility(View.GONE);
                                    }
                                    // Done getting all the OnMyMinds stored in Firestore Database.
                                    Log.d(TAG, "*** Done getting data from Firestore ***");
                                    progressBar.setVisibility(View.GONE);
                                    ommsRecyclerView.setVisibility(View.VISIBLE);
                                } else {
                                    // Error occured when retrieving OnMyMinds.
                                    Log.e(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        }).start();
    }
}
