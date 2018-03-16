package dev.danielholmberg.improve.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Activities.AddOnMyMindActivity;
import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.ViewHolders.OnMyMindViewHolder;

/**
 * Created by DanielHolmberg on 2018-01-20.
 */

public class OnMyMindFragment extends Fragment {
    private static final String TAG = "OnMyMindFragment";

    private Improve app;
    private FirebaseStorageManager storageManager;
    private DatabaseReference onMyMindsRef;

    private View view;
    private RecyclerView ommsRecyclerView;
    private List<OnMyMind> onMyMindList = new ArrayList<>();
    private FirebaseRecyclerAdapter recyclerAdapter;
    private TextView emptyListText;
    private FloatingActionButton fab;

    public OnMyMindFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        storageManager = app.getFirebaseStorageManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_omms,
                container, false);

        // Initialize View components to be used.
        ommsRecyclerView = (RecyclerView) view.findViewById(R.id.omms_list);
        emptyListText = (TextView) view.findViewById(R.id.empty_omms_list_tv);
        fab = (FloatingActionButton) view.findViewById(R.id.add_omm);

        // Initialize the LinearLayoutManager
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        ommsRecyclerView.setLayoutManager(recyclerLayoutManager);

        // Initialize the adapter for RecyclerView.
        initAdapter();
        ommsRecyclerView.setAdapter(recyclerAdapter);

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
                addOnMyMind();
            }
        });

        return view;
    }

    private void initAdapter() {
        Query query = storageManager.getOnMyMindsRef().orderByChild("isDone");

        FirebaseRecyclerOptions<OnMyMind> options =
                new FirebaseRecyclerOptions.Builder<OnMyMind>()
                        .setQuery(query, OnMyMind.class)
                        .build();

        recyclerAdapter = new FirebaseRecyclerAdapter<OnMyMind, OnMyMindViewHolder>(options) {
            @Override
            public OnMyMindViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_onmymind, parent, false);

                return new OnMyMindViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(OnMyMindViewHolder holder, int position, OnMyMind model) {
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

    /**
     * Called when a user clicks on the Floating Action Button to add a new OnMyMind.
     */
    private void addOnMyMind() {
        Intent addOnMyMindIntent = new Intent(getContext(), AddOnMyMindActivity.class);
        startActivity(addOnMyMindIntent);
    }
}
