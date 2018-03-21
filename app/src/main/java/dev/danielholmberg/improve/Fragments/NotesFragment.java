package dev.danielholmberg.improve.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.Query;

import dev.danielholmberg.improve.Activities.AddNoteActivity;
import dev.danielholmberg.improve.Components.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.R;

/**
 * Created by DanielHolmberg on 2018-01-20.
 */

public class NotesFragment extends Fragment {
    private static final String TAG = "NotesFragment";

    private Improve app;
    private FirebaseStorageManager storageManager;

    private View view;
    private RecyclerView notesRecyclerView;
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
        storageManager = app.getFirebaseStorageManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes,
                container, false);

        // Initialize View components to be used.
        notesRecyclerView = (RecyclerView) view.findViewById(R.id.notes_list);
        emptyListText = (TextView) view.findViewById(R.id.empty_notes_list_tv);
        fab = (FloatingActionButton) view.findViewById(R.id.add_note);

        // Initialize the LinearLayoutManager
        LinearLayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        notesRecyclerView.setLayoutManager(recyclerLayoutManager);

        // Setting RecyclerAdapter to RecyclerList.
        initAdapter();
        notesRecyclerView.setAdapter(recyclerAdapter);

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

    private void initAdapter() {
        Query query = storageManager.getNotesRef().orderByChild("isDone");

        FirebaseRecyclerOptions<Note> options =
                new FirebaseRecyclerOptions.Builder<Note>()
                        .setQuery(query, Note.class)
                        .build();

        recyclerAdapter = new FirebaseRecyclerAdapter<Note, NoteViewHolder>(options) {
            @Override
            public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_note, parent, false);

                return new NoteViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note model) {
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

        public NoteViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            context = itemView.getContext();
        }

        public void bindModelToView(final Note note) {
            // Body views
            TextView marker = (TextView) mView.findViewById(R.id.item_note_marker_iv);
            TextView title = (TextView) mView.findViewById(R.id.item_note_title_tv);
            TextView info = (TextView) mView.findViewById(R.id.item_note_info_tv);

            // Footer views
            View footer = (View) mView.findViewById(R.id.footer_note);
            TextView doneMarker = (TextView) mView.findViewById(R.id.done_mark);
            TextView timestamp = (TextView) mView.findViewById(R.id.footer_note_timestamp_tv);

            // Setting Note content.
            marker.setBackgroundColor(Color.parseColor(note.getColor()));
            title.setText(note.getTitle());
            info.setText(note.getInfo());
            timestamp.setText(note.getTimestamp());

            // Hiding Note Info and Footer.
            // Showing a mark to symbolize that the Note is completed.
            if(note.getIsDone()) {
                info.setVisibility(View.GONE);
                footer.setVisibility(View.GONE);
                doneMarker.setVisibility(View.VISIBLE);
            }

            // Setting OnClickListener on the Root View.
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NoteDetailsDialogFragment noteDetailsDialogFragment = new NoteDetailsDialogFragment();
                    noteDetailsDialogFragment.setArguments(createBundle(note, getAdapterPosition()));
                    noteDetailsDialogFragment.show(((AppCompatActivity)context).getSupportFragmentManager(),
                            noteDetailsDialogFragment.getTag());
                }
            });
        }

        private Bundle createBundle(Note note, int itemPos) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("note", note);
            bundle.putInt("position", itemPos);
            bundle.putBoolean("isDone", note.getIsDone());
            return bundle;
        }
    }
}
