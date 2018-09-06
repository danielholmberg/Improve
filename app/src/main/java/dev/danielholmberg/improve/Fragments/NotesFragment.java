package dev.danielholmberg.improve.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;

import dev.danielholmberg.improve.Activities.AddNoteActivity;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-20.
 */

public class NotesFragment extends Fragment {
    private static final String TAG = NotesFragment.class.getSimpleName();

    private Improve app;
    private FirebaseDatabaseManager storageManager;

    private View view;
    private RecyclerView notesRecyclerView;
    private String noteListOrderBy = "timestampUpdated";
    private TextView emptyListText;
    private FloatingActionButton fab;

    private FirebaseRecyclerAdapter recyclerAdapter;

    public NotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        app.setNotesFragmentRef(this);
        storageManager = app.getFirebaseDatabaseManager();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes,
                container, false);

        // Initialize View components to be used.
        notesRecyclerView = (RecyclerView) view.findViewById(R.id.notes_list);
        emptyListText = (TextView) view.findViewById(R.id.empty_notes_list_tv);
        fab = (FloatingActionButton) view.findViewById(R.id.add_note);

        // Initialize the LinearLayoutManager
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerLayoutManager.setReverseLayout(true);
        recyclerLayoutManager.setStackFromEnd(true);
        notesRecyclerView.setLayoutManager(recyclerLayoutManager);

        // Setting RecyclerAdapter to RecyclerList.
        setUpAdapter();

        // Add a OnScrollListener to change when to show the Floating Action Button for adding
        // a new Note.
        notesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
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
                addNote();
            }
        });

        return view;
    }

    private void setUpAdapter() {
        Query query = storageManager.getNotesRef().orderByChild(noteListOrderBy);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    notesRecyclerView.setVisibility(View.VISIBLE);
                    emptyListText.setVisibility(View.GONE);
                } else {
                    notesRecyclerView.setVisibility(View.GONE);
                    emptyListText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Crashlytics.log("Failed to retrieve Firebase data for NotesRef: " + databaseError);
            }
        });

        FirebaseRecyclerOptions<Note> options =
                new FirebaseRecyclerOptions.Builder<Note>()
                        .setQuery(query, Note.class)
                        .build();

        recyclerAdapter = new FirebaseRecyclerAdapter<Note, NoteViewHolder>(options) {

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_note, parent, false);

                return new NoteViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note model) {
                holder.bindModelToView(model);
            }
        };

        recyclerAdapter.startListening();
        notesRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_notes, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_notes_by_title_alphabetical:
                sortNotesByTitle();
                return true;
            case R.id.sort_notes_by_timestamp_updated:
                sortNotesByLastUpdated();
                return true;
            case R.id.sort_notes_by_marker:
                sortNotesByMarker();
                return true;
            default:
                break;
        }
        return false;
    }

    private void sortNotesByMarker() {
        noteListOrderBy = "color";
        setUpAdapter();
    }

    private void sortNotesByLastUpdated() {
        noteListOrderBy = "timestampUpdated";
        setUpAdapter();
    }

    private void sortNotesByTitle() {
        noteListOrderBy = "title";
        setUpAdapter();
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
     * Called when a user clicks on the Floating Action Button to add a new Note.
     */
    private void addNote() {
        Intent addNoteIntent = new Intent(getContext(), AddNoteActivity.class);
        startActivity(addNoteIntent);
    }

    /**
     * ViewHolder class for each RecyclerList item.
     */
    public class NoteViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private Context context;
        private Note note;

        public NoteViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            context = itemView.getContext();
        }

        // OBS! Due to RecyclerView:
        // We need to define all views of each note!
        // Otherwise each note view won't be unique.
        public void bindModelToView(final Note note) {
            this.note = note;

            // [START] All views of a contact
            LinearLayout marker = (LinearLayout) mView.findViewById(R.id.item_note_marker);
            TextView title = (TextView) mView.findViewById(R.id.item_note_title_tv);
            TextView info = (TextView) mView.findViewById(R.id.item_note_info_tv);

            View footer = (View) mView.findViewById(R.id.footer_note);
            TextView timestamp = (TextView) mView.findViewById(R.id.footer_note_timestamp_tv);
            // [END] All views of a note

            // [START] Define each view
            try {
                marker.setBackgroundColor(note.getColor() != null ? Color.parseColor(note.getColor()) :
                        getResources().getColor(R.color.noColor));
            } catch (Exception e) {
                marker.setBackgroundColor(getResources().getColor(R.color.noColor));
            }
            title.setText(note.getTitle());
            info.setText(note.getInfo());
            timestamp.setText(tranformMillisToDateSring(Long.parseLong(note.getTimestampUpdated())));

            if(note.getArchived()) {
                info.setVisibility(View.GONE);
                footer.setVisibility(View.GONE);
                marker.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else {
                if(info.getText().toString().trim().isEmpty()){
                    info.setVisibility(View.GONE);
                } else {
                    info.setVisibility(View.VISIBLE);
                }
                footer.setVisibility(View.VISIBLE);
            }
            // [END] Define each view

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showNoteDetailDialog();
                }
            });
        }

        private void showNoteDetailDialog() {
            FragmentManager fm = getFragmentManager();
            NoteDetailsDialogFragment noteDetailsDialogFragment = NoteDetailsDialogFragment.newInstance();
            noteDetailsDialogFragment.setContext(context);
            noteDetailsDialogFragment.setArguments(createBundle(note, getAdapterPosition()));
            
            noteDetailsDialogFragment.show(fm, NoteDetailsDialogFragment.TAG);
        }

        private String tranformMillisToDateSring(long timeInMillis) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeInMillis);

            return DateFormat.getDateTimeInstance().format(calendar.getTime());
        }

        private Bundle createBundle(Note note, int itemPos) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(NoteDetailsDialogFragment.NOTE_KEY, note);
            bundle.putInt(NoteDetailsDialogFragment.NOTE_PARENT_FRAGMENT_KEY, R.integer.NOTES_FRAGMENT);
            bundle.putInt(NoteDetailsDialogFragment.NOTE_ADAPTER_POS_KEY, itemPos);
            return bundle;
        }
    }
}
