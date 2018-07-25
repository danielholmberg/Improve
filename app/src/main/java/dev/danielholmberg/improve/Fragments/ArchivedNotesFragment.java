package dev.danielholmberg.improve.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;

import dev.danielholmberg.improve.Activities.NoteActivity;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-20.
 */

public class ArchivedNotesFragment extends Fragment {
    private static final String TAG = ArchivedNotesFragment.class.getSimpleName();

    private Improve app;
    private FirebaseStorageManager storageManager;

    private View view;
    private RecyclerView archivedNotesRecyclerView;

    private FirebaseRecyclerAdapter recyclerAdapter;

    public ArchivedNotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = Improve.getInstance();
        app.setArchivedNotesFragmentRef(this);
        storageManager = app.getFirebaseStorageManager();
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

        // Initialize the LinearLayoutManager
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        archivedNotesRecyclerView.setLayoutManager(recyclerLayoutManager);

        // Setting RecyclerAdapter to RecyclerList.
        initAdapter();
        archivedNotesRecyclerView.setAdapter(recyclerAdapter);

        return view;
    }

    private void initAdapter() {
        Query query = storageManager.getArchivedNotesRef().orderByChild("isDone");

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

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_notes, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_notes_by_title_alphabetical:
                // TODO: Sort notes by title alphabetical
                sortNotesByTitle();
                return true;
            case R.id.sort_notes_by_timestamp:
                // TODO: Sort note by timestamp
                sortNotesByTimestamp();
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
        Toast.makeText(app, "Sorted by marker color", Toast.LENGTH_SHORT).show();
    }

    private void sortNotesByTimestamp() {
        Toast.makeText(app, "Sorted by timestamp", Toast.LENGTH_SHORT).show();

    }

    private void sortNotesByTitle() {
        Toast.makeText(app, "Sorted by title", Toast.LENGTH_SHORT).show();
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

        public ArchivedNoteViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            context = itemView.getContext();
        }

        // OBS! Due to RecyclerView:
        // We need to define all views of each note!
        // Otherwise each note view won't be unique.
        public void bindModelToView(final Note note) {

            // [START] All views of a contact
            LinearLayout marker = (LinearLayout) mView.findViewById(R.id.item_archived_note_marker);
            TextView title = (TextView) mView.findViewById(R.id.item_archived_note_title_tv);
            TextView doneMarker = (TextView) mView.findViewById(R.id.archived_done_mark);
            // [END] All views of a note

            // [START] Define each view
            marker.setBackgroundColor(Color.parseColor(note.getColor()));
            title.setText(note.getTitle());
            // [END] Define each view

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent showNoteDetails = new Intent(context, NoteActivity.class);
                    Bundle noteBundle = createBundle(note, getAdapterPosition());
                    showNoteDetails.putExtra("noteBundle", noteBundle);
                    startActivity(showNoteDetails);
                }
            });
        }

        private Bundle createBundle(Note note, int itemPos) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("note", note);
            bundle.putInt("position", itemPos);
            bundle.putInt("parentFragment", R.integer.ARCHIVED_NOTES_FRAGMENT);
            return bundle;
        }
    }
}
