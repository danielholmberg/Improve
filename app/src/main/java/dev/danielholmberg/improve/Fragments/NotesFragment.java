package dev.danielholmberg.improve.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import dev.danielholmberg.improve.Activities.AddNoteActivity;
import dev.danielholmberg.improve.Models.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-20.
 */

public class NotesFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static final String TAG = NotesFragment.class.getSimpleName();

    private Improve app;
    private FirebaseDatabaseManager databaseManager;
    private Context context;

    private View view;
    private CoordinatorLayout snackbarView;
    private RecyclerView notesRecyclerView;
    private LinearLayoutManager recyclerLayoutManager;
    private TextView emptyListText;
    private FloatingActionButton fab;

    public NotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        app.setNotesFragmentRef(this);
        this.context = getContext();
        databaseManager = app.getFirebaseDatabaseManager();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes,
                container, false);

        snackbarView = view.findViewById(R.id.note_fragment_container);

        notesRecyclerView = (RecyclerView) view.findViewById(R.id.notes_list);
        emptyListText = (TextView) view.findViewById(R.id.empty_notes_list_tv);
        fab = (FloatingActionButton) view.findViewById(R.id.add_note);

        recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerLayoutManager.setReverseLayout(true);
        recyclerLayoutManager.setStackFromEnd(true);
        notesRecyclerView.setLayoutManager(recyclerLayoutManager);

        notesRecyclerView.setAdapter(app.getNotesAdapter());

        initListScrollListener();
        initListDataChangeListener();

        return view;
    }

    private void initListScrollListener() {
        notesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.isShown()) {
                    // Hide the FAB when the user scrolls down.
                    fab.hide();
                }

                if(!recyclerView.canScrollVertically(-1)) {
                    // we have reached the top of the list
                    app.getMainActivityRef().findViewById(R.id.toolbar_dropshadow).setVisibility(View.GONE);
                } else {
                    // we are not at the top yet
                    app.getMainActivityRef().findViewById(R.id.toolbar_dropshadow).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Show the FAB when the user has stopped scrolling.
                    fab.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNote();
            }
        });
    }

    private void initListDataChangeListener() {
        databaseManager.getNotesRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(app.getNotesAdapter().getItemCount() > 0) {
                    notesRecyclerView.setVisibility(View.VISIBLE);
                    emptyListText.setVisibility(View.GONE);

                    // Scroll to the "top" (bottom) to show changed Note.
                    notesRecyclerView.scrollToPosition(app.getNotesAdapter().getItemCount()-1);
                } else {
                    notesRecyclerView.setVisibility(View.GONE);
                    emptyListText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                final Note removedNote = dataSnapshot.getValue(Note.class);

                if(removedNote != null) {
                    if(app.getArchivedNotes().containsKey(removedNote.getId())) {
                        // Note is Archived and not truly deleted.
                        Snackbar.make(snackbarView,
                                "Note archived", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        databaseManager.unarchiveNote(removedNote);
                                    }
                                }).show();
                    } else {
                        Snackbar.make(snackbarView,
                                "Note deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        databaseManager.addNote(removedNote);
                                    }
                                }).show();
                    }
                }

                if(app.getNotesAdapter().getItemCount() > 0) {
                    notesRecyclerView.setVisibility(View.VISIBLE);
                    emptyListText.setVisibility(View.GONE);
                } else {
                    notesRecyclerView.setVisibility(View.GONE);
                    emptyListText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /**
     * Called when a user clicks on the Floating Action Button to add a new Note.
     */
    private void addNote() {
        Intent addNoteIntent = new Intent(getContext(), AddNoteActivity.class);
        startActivity(addNoteIntent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_notes, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_note:
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setQueryHint("Search Note");
                searchView.setOnQueryTextListener(this);
                searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        Log.d(TAG, "Search closed!");
                        app.getNotesAdapter().clearFilter();
                        return true;
                    }
                });

                EditText searchEditText = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
                searchEditText.setTextColor(getResources().getColor(R.color.search_text_color));
                searchEditText.setHintTextColor(getResources().getColor(R.color.search_hint_color));
                searchEditText.setCursorVisible(false);

                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem menuItem) {
                        Log.d(TAG, "Search opened!");
                        app.getNotesAdapter().initSearch();
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                        Log.d(TAG, "Search closed!");
                        app.getNotesAdapter().clearFilter();
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

        app.getNotesAdapter().filter(newText);
        notesRecyclerView.scrollToPosition(app.getNotesAdapter().getItemCount()-1);

        return true;
    }
}
