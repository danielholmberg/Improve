package dev.danielholmberg.improve.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;

import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-20.
 */

public class ArchivedNotesFragment extends Fragment {
    private static final String TAG = ArchivedNotesFragment.class.getSimpleName();

    private Improve app;
    private FirebaseDatabaseManager storageManager;

    private View view;
    private RecyclerView archivedNotesRecyclerView;
    private TextView emptyListText;

    private String noteListOrderBy = "timestamp";
    private FirebaseRecyclerAdapter recyclerAdapter;

    public ArchivedNotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        app.setArchivedNotesFragmentRef(this);
        storageManager = app.getFirebaseDatabaseManager();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_archived_notes,
                container, false);

        // Initialize View components to be used.
        archivedNotesRecyclerView = (RecyclerView) view.findViewById(R.id.archived_notes_list);
        emptyListText = (TextView) view.findViewById(R.id.empty_archive_list_tv);

        // Initialize the LinearLayoutManager
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerLayoutManager.setReverseLayout(true);
        recyclerLayoutManager.setStackFromEnd(true);
        archivedNotesRecyclerView.setLayoutManager(recyclerLayoutManager);

        // Setting RecyclerAdapter to RecyclerList.
        setUpAdapter();

        return view;
    }

    private void setUpAdapter() {
        Query query = storageManager.getArchivedNotesRef().orderByChild(noteListOrderBy);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    archivedNotesRecyclerView.setVisibility(View.VISIBLE);
                    emptyListText.setVisibility(View.GONE);
                } else {
                    archivedNotesRecyclerView.setVisibility(View.GONE);
                    emptyListText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions<Note> options =
                new FirebaseRecyclerOptions.Builder<Note>()
                        .setQuery(query, Note.class)
                        .build();

        recyclerAdapter = new FirebaseRecyclerAdapter<Note, ArchivedNoteViewHolder>(options) {
            @Override
            public ArchivedNoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_archived_note, parent, false);

                return new ArchivedNoteViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ArchivedNoteViewHolder holder, int position, @NonNull Note model) {
                holder.bindModelToView(model);
            }
        };

        recyclerAdapter.startListening();
        archivedNotesRecyclerView.setAdapter(recyclerAdapter);

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
     * ViewHolder class for each RecyclerList item.
     */
    public class ArchivedNoteViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private Context context;
        private Note note;

        public ArchivedNoteViewHolder(View itemView) {
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
            LinearLayout marker = (LinearLayout) mView.findViewById(R.id.item_archived_note_marker);
            TextView title = (TextView) mView.findViewById(R.id.item_archived_note_title_tv);
            // [END] All views of a note

            // [START] Define each view
            marker.setBackgroundColor(Color.parseColor(note.getColor()));
            title.setText(note.getTitle());
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

            /*
            // SETS the target fragment for use later when sending results
            noteDetailsDialogFragment.setTargetFragment(NotesFragment.this, 300);
            */

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
            bundle.putInt(NoteDetailsDialogFragment.NOTE_PARENT_FRAGMENT_KEY, R.integer.ARCHIVED_NOTES_FRAGMENT);
            bundle.putInt(NoteDetailsDialogFragment.NOTE_ADAPTER_POS_KEY, itemPos);
            return bundle;
        }
    }
}
