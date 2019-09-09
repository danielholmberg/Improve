package dev.danielholmberg.improve.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dev.danielholmberg.improve.Models.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.ViewHolders.NoteViewHolder;

public class NotesAdapter extends RecyclerView.Adapter<NoteViewHolder> {
    private static final String TAG = NotesAdapter.class.getSimpleName();

    private Improve app;
    private FirebaseDatabaseManager databaseManager;
    private SortedList<Note> notes;
    private List<Note> notesCopy, filteredNotes;

    public NotesAdapter() {
        this.app = Improve.getInstance();
        this.databaseManager = app.getFirebaseDatabaseManager();

        notes = new SortedList<>(Note.class, new SortedList.Callback<Note>() {
            @Override
            public int compare(@NonNull Note o1, @NonNull Note o2) {
                return o1.getId().compareTo(o2.getId());
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                boolean tagsNotSame = false;
                for(String oldTagId: oldItem.getTags().keySet()) {
                    for(String newTagId: newItem.getTags().keySet()) {
                        if(!oldTagId.equals(newTagId)) {
                            tagsNotSame = true;
                            break;
                        }
                    }
                }

                return oldItem.getTitle().equals(newItem.getTitle())
                        && oldItem.getInfo().equals(newItem.getInfo())
                        && tagsNotSame;
            }

            @Override
            public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });

        initDatabaseListener();

    }

    /**
     * Downloads all notes from the Notes-node and adds a childEventListener to detect changes.
     */
    private void initDatabaseListener() {
        databaseManager.getNotesRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // This method is triggered when a new child is added
                // to the location to which this listener was added.
                Note addedNote = dataSnapshot.getValue(Note.class);

                if(addedNote != null) {
                    notes.add(addedNote);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // This method is triggered when the data at a child location has changed.
                Note updatedNote = dataSnapshot.getValue(Note.class);

                if(updatedNote != null) {
                    notes.add(updatedNote);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // This method is triggered when a child is removed from the location
                // to which this listener was added.
                final Note removedNote = dataSnapshot.getValue(Note.class);

                if(removedNote != null) {
                    notes.remove(removedNote);
                } else {
                    Toast.makeText(app, "Failed to delete note, please try again later",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // This method is triggered when a child location's priority changes.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // This method will be triggered in the event that this listener either failed
                // at the server, or is removed as a result of the security and Firebase rules.

                Log.e(TAG, "Notes ChildEventListener cancelled: " + databaseError);
            }
        });

    }

    public void add(Note note) {
        notes.add(note);
    }

    public void initSearch() {
        notesCopy = getNotesList();
        filteredNotes = new ArrayList<>();
    }

    public void clearFilter() {
        for(Note note: filteredNotes) {
            notes.add(note);
        }
        notifyDataSetChanged();
    }

    public void filter(String queryText) {
        final String lowerCaseQuery = queryText.toLowerCase();

        for (Note note : notesCopy) {
            if (!note.getTitle().toLowerCase().contains(lowerCaseQuery) &&
                    !note.getInfo().toLowerCase().contains(lowerCaseQuery)) {
                notes.remove(note);
                filteredNotes.add(note);
            } else {
                notes.add(note);
                filteredNotes.remove(note);
            }
            notifyDataSetChanged();
        }
    }

    public List<Note> getNotesList() {
        List<Note> notesCopy = new ArrayList<>();
        for(int i = 0; i < notes.size(); i++) {
            notesCopy.add(notes.get(i));
        }
        return notesCopy;
    }

    public HashMap<String, Object> getHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        for(int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            hashMap.put(note.getId(), note);
        }
        return hashMap;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);

        return new NoteViewHolder(parent.getContext(), view, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bindModelToView(notes.get(position));
    }


    @Override
    public int getItemCount() {
        return notes.size();
    }
}
