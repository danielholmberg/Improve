package dev.danielholmberg.improve.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import dev.danielholmberg.improve.Models.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-20.
 */

public class ArchivedNotesFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static final String TAG = ArchivedNotesFragment.class.getSimpleName();

    private Improve app;
    private FirebaseDatabaseManager databaseManager;

    private View view;
    private CoordinatorLayout snackbarView;
    private RecyclerView archivedNotesRecyclerView;
    private TextView emptyListText;

    public ArchivedNotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        app.setArchivedNotesFragmentRef(this);
        databaseManager = app.getFirebaseDatabaseManager();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_archived_notes,
                container, false);

        snackbarView = view.findViewById(R.id.archivednote_fragment_container);

        archivedNotesRecyclerView = (RecyclerView) view.findViewById(R.id.archived_notes_list);
        emptyListText = (TextView) view.findViewById(R.id.empty_archive_list_tv);

        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerLayoutManager.setReverseLayout(true);
        recyclerLayoutManager.setStackFromEnd(true);
        archivedNotesRecyclerView.setLayoutManager(recyclerLayoutManager);

        archivedNotesRecyclerView.setAdapter(app.getArchivedNotesAdapter());

        initListScrollListener();
        initListDataChangeListener();

        return view;
    }

    private void initListScrollListener() {
        archivedNotesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(!recyclerView.canScrollVertically(-1)) {
                    // we have reached the top of the list
                    Log.d(TAG, "Reached the top!");
                    app.getMainActivityRef().findViewById(R.id.toolbar_dropshadow).setVisibility(View.GONE);
                } else {
                    // we are not at the top yet
                    Log.d(TAG, "not at top yet!");
                    app.getMainActivityRef().findViewById(R.id.toolbar_dropshadow).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initListDataChangeListener() {
        databaseManager.getArchivedNotesRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(app.getArchivedNotesAdapter().getItemCount() > 0) {
                    archivedNotesRecyclerView.setVisibility(View.VISIBLE);
                    emptyListText.setVisibility(View.GONE);
                } else {
                    archivedNotesRecyclerView.setVisibility(View.GONE);
                    emptyListText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                final Note removedArchivedNote = dataSnapshot.getValue(Note.class);

                if(removedArchivedNote != null) {
                    if(app.getNotes().containsKey(removedArchivedNote.getId())) {
                        // Note is Unarchived and not truly deleted.
                        Snackbar.make(snackbarView,
                                "Note unarchived", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        databaseManager.archiveNote(removedArchivedNote);
                                    }
                                }).show();
                    } else {
                        Snackbar.make(snackbarView,
                                "Note deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        databaseManager.addArchivedNote(removedArchivedNote);
                                    }
                                }).show();
                    }
                }

                if(app.getArchivedNotesAdapter().getItemCount() > 0) {
                    archivedNotesRecyclerView.setVisibility(View.VISIBLE);
                    emptyListText.setVisibility(View.GONE);
                } else {
                    archivedNotesRecyclerView.setVisibility(View.GONE);
                    emptyListText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_archived_notes, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_archived_note:
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setQueryHint("Search Archived Note");
                searchView.setOnQueryTextListener(this);

                EditText searchEditText = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
                searchEditText.setTextColor(getResources().getColor(R.color.search_text_color));
                searchEditText.setHintTextColor(getResources().getColor(R.color.search_hint_color));
                searchEditText.setCursorVisible(false);

                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem menuItem) {
                        Log.d(TAG, "Search opened!");
                        app.getArchivedNotesAdapter().initSearch();
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                        Log.d(TAG, "Search closed!");
                        app.getArchivedNotesAdapter().clearFilter();
                        return true;
                    }
                });

                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d(TAG, "Query Submitted: " + query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(TAG, "Query Inserted: " + newText);

        app.getArchivedNotesAdapter().filter(newText);
        archivedNotesRecyclerView.scrollToPosition(app.getArchivedNotesAdapter().getItemCount()-1);

        return true;
    }
}
