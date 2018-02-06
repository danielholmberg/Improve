package dev.danielholmberg.improve.Fragments;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Adapters.OnMyMindsRecyclerViewAdapter;
import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.InternalStorage;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-20.
 */

public class ViewOnMyMindFragment extends Fragment {
    private static final String TAG = "ViewOnMyMindFragment";
    private static final String INTERNAL_STORAGE_KEY = "OnMyMinds";

    private FirebaseFirestore firestoreDB;
    private RecyclerView ommsRecyclerView;
    private List<OnMyMind> onmymindList = new ArrayList<>();
    private List<OnMyMind> cachedOnMyMinds;
    private OnMyMindsRecyclerViewAdapter recyclerViewAdapter;
    private TextView emptyListText;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fab;

    public ViewOnMyMindFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_omms,
                container, false);

        // Initialize Firestore Database.
        firestoreDB = FirebaseFirestore.getInstance();

        // Initialize View components to be used.
        ommsRecyclerView = (RecyclerView) view.findViewById(R.id.omms_list);
        emptyListText = (TextView) view.findViewById(R.id.empty_list_text);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh_omms);
        fab = (FloatingActionButton) getActivity().getWindow().findViewById(R.id.add_omm);

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

        // Add a OnScrollListener to change when to show the Floating Action Button for adding a new OnMyMind.
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
            cachedOnMyMinds = (List<OnMyMind>) InternalStorage.readObject(getContext(), INTERNAL_STORAGE_KEY);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(cachedOnMyMinds != null) {
            // The Internal Storage list has been initialized in a previous session.
            if(cachedOnMyMinds.isEmpty()) {
                // The already stored list of OnMyMinds is empty.
                // Get the list from the Firestore Database instead.
                Log.d(TAG, "Getting OnMyMinds from Firestore Database");
                getDataFromFirestore();
            } else {
                // The list of already stored list of OnMyMinds is NOT empty.
                // Add the stored list of OnMyMinds to the custom RecycleViewAdapter.
                Log.d(TAG, "Getting OnMyMinds from Internal Storge");
                recyclerViewAdapter.setAdapterList(cachedOnMyMinds);
                recyclerViewAdapter.notifyDataSetChanged();
                ommsRecyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            // The Internal Storage list has NOT been initialized in a previous session.
            // Get the list from the Firestore Database instead.
            Log.d(TAG, "Getting OnMyMinds from Firestore Database");
            getDataFromFirestore();
        }
    }

    /**
     * Retrieves OnMyMind list data from Firestore Database
     */
    private void getDataFromFirestore() {
        ommsRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        // Run the Query on a new thread for performance purposes.
        new Thread(new Runnable() {
            @Override
            public void run() {
                firestoreDB.collection("onmyminds")
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
                                        emptyListText.setVisibility(View.VISIBLE);
                                    } else {
                                        // Getting data from each stored OnMyMind.
                                        for (DocumentSnapshot onmymind : task.getResult()) {
                                            OnMyMind omm = onmymind.toObject(OnMyMind.class);
                                            omm.setID(onmymind.getId());
                                            onmymindList.add(omm);
                                        }
                                        try {
                                            // Writing all the retrived OnMyMinds to Internal Storage for offline use.
                                            InternalStorage.writeObject(getContext(), INTERNAL_STORAGE_KEY, onmymindList);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        Log.d(TAG, "Wrote OnMyMinds to Internal Storage");
                                        recyclerViewAdapter.setAdapterList(onmymindList);
                                        recyclerViewAdapter.notifyDataSetChanged();
                                        emptyListText.setVisibility(View.GONE);
                                    }
                                    // Done getting all the OnMyMinds stored in Firestore Database.
                                    Log.d(TAG, "Done loading OnMyMinds");
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
