package dev.danielholmberg.improve.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;

import dev.danielholmberg.improve.Adapters.OnMyMindsRecyclerViewAdapter;
import dev.danielholmberg.improve.Components.OnMyMind;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-20.
 */

public class ViewOnMyMindFragment extends Fragment {
    private static final String TAG = "ViewOnMyMindFragment";
    private FirebaseFirestore firestoreDB;
    private RecyclerView ommsRecyclerView;
    private List<OnMyMind> onmymindList = new ArrayList<>();
    private OnMyMindsRecyclerViewAdapter recyclerViewAdapter;
    private TextView emptyListText;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    public ViewOnMyMindFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_omms,
                container, false);

        firestoreDB = FirebaseFirestore.getInstance();

        ommsRecyclerView = (RecyclerView) view.findViewById(R.id.omms_list);
        emptyListText = (TextView) view.findViewById(R.id.empty_list_text);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        recyclerViewAdapter = new
                OnMyMindsRecyclerViewAdapter(onmymindList,
                getActivity(), firestoreDB);
        ommsRecyclerView.setAdapter(recyclerViewAdapter);

        LinearLayoutManager recyclerLayoutManager =
                new LinearLayoutManager(getActivity().getApplicationContext());
        ommsRecyclerView.setLayoutManager(recyclerLayoutManager);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh_omms);
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

    private void getDocumentsFromCollection() {
        ommsRecyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        firestoreDB.collection("onmyminds")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            onmymindList = new ArrayList<>();
                            if(task.getResult().isEmpty()){
                                Log.e(TAG, "No OnMyMinds exists");
                                emptyListText.setVisibility(View.VISIBLE);
                            } else {
                                for (DocumentSnapshot onmymind : task.getResult()) {
                                    OnMyMind omm = onmymind.toObject(OnMyMind.class);
                                    omm.setID(onmymind.getId());
                                    onmymindList.add(omm);
                                }
                                recyclerViewAdapter.setAdapterList(onmymindList);
                                recyclerViewAdapter.notifyDataSetChanged();
                                emptyListText.setVisibility(View.GONE);
                            }
                            Log.d(TAG, "Done loading OnMyMinds");
                            progressBar.setVisibility(View.GONE);
                            ommsRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
