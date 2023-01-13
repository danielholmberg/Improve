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
import dev.danielholmberg.improve.Managers.DatabaseManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.ViewHolders.ArchivedNoteViewHolder;

public class ArchivedNotesAdapter extends RecyclerView.Adapter<ArchivedNoteViewHolder> {
    private static final String TAG = ArchivedNotesAdapter.class.getSimpleName();

    private Improve app;
    private DatabaseManager databaseManager;
    private SortedList<Note> archivedNotes;
    private List<Note> archivedNotesCopy, filteredArchivedNotes;

    public ArchivedNotesAdapter() {
        this.app = Improve.getInstance();
        this.databaseManager = app.getDatabaseManager();

        archivedNotes = new SortedList<Note>(Note.class, new SortedList.Callback<Note>() {
            @Override
            public int compare(@NonNull Note o1, @NonNull Note o2) {
                // Makes sure that the objects has a value for parameter "updated".
                // Those with a value are greater than those without.
                // This issue is only related to Notes created with v1.
                if(o1.getUpdated() == null && o2.getUpdated() == null) {
                    return 0;
                } else if(o1.getUpdated() != null && o2.getUpdated() == null) {
                    return 1;
                } else if(o1.getUpdated() == null && o2.getUpdated() != null) {
                    return -1;
                } else {
                    return o1.getUpdated().compareTo(o2.getUpdated());
                }
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                return oldItem.getTitle().trim().equals(newItem.getTitle().trim())
                        && oldItem.getInfo().trim().equals(newItem.getInfo().trim())
                        && oldItem.getTags().size() != newItem.getTags().size();
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
     * Downloads all archivedNotes from the Notes-node and adds a childEventListener to detect changes.
     */
    private void initDatabaseListener() {
        databaseManager.getArchivedNotesRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // This method is triggered when a new child is added
                // to the location to which this listener was added.
                Note archivedNote = dataSnapshot.getValue(Note.class);

                if(archivedNote != null) {
                    archivedNotes.add(archivedNote);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // This method is triggered when the data at a child location has changed.
                Note updatedNote = dataSnapshot.getValue(Note.class);

                if(updatedNote != null) {
                    Note existingNote = (Note) getHashMap().get(updatedNote.getId());
                    if(existingNote == null) {
                        archivedNotes.add(updatedNote);
                    } else {
                        archivedNotes.updateItemAt(getArchivedNotesList().indexOf(existingNote), updatedNote);
                    }

                    Toast.makeText(app, "Archived note updated", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // This method is triggered when a child is removed from the location
                // to which this listener was added.
                final Note removedNote = dataSnapshot.getValue(Note.class);

                if(removedNote != null) {
                    archivedNotes.remove(removedNote);
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

                Log.e(TAG, "ArchivedNotes ChildEventListener cancelled: " + databaseError);
            }
        });

    }

    public void initSearch() {
        archivedNotesCopy = getArchivedNotesList();
        filteredArchivedNotes = new ArrayList<>();
    }

    public void clearFilter() {
        for(Note note: filteredArchivedNotes) {
            archivedNotes.add(note);
        }
        notifyDataSetChanged();
    }

    public void filter(String queryText) {
        final String lowerCaseQuery = queryText.toLowerCase();

        for (Note note : archivedNotesCopy) {
            if (!note.getTitle().toLowerCase().contains(lowerCaseQuery) &&
                    !note.getInfo().toLowerCase().contains(lowerCaseQuery)) {
                archivedNotes.remove(note);
                filteredArchivedNotes.add(note);
            } else {
                archivedNotes.add(note);
                filteredArchivedNotes.remove(note);
            }
            notifyDataSetChanged();
        }
    }

    public List<Note> getArchivedNotesList() {
        List<Note> archivedNotesCopy = new ArrayList<>();
        for(int i = 0; i < archivedNotes.size(); i++) {
            archivedNotesCopy.add(archivedNotes.get(i));
        }
        return archivedNotesCopy;
    }

    public HashMap<String, Object> getHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        for(int i = 0; i < archivedNotes.size(); i++) {
            Note note = archivedNotes.get(i);
            hashMap.put(note.getId(), note);
        }
        return hashMap;
    }

    @Override
    public int getItemCount() {
        return archivedNotes.size();
    }

    @NonNull
    @Override
    public ArchivedNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_archived_note, parent, false);

        return new ArchivedNoteViewHolder(parent.getContext(), view, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ArchivedNoteViewHolder holder, int position) {
        holder.bindModelToView(archivedNotes.get(position));
    }
}
