package dev.danielholmberg.improve.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.danielholmberg.improve.Callbacks.FirebaseStorageCallback;
import dev.danielholmberg.improve.Managers.FirebaseStorageManager;
import dev.danielholmberg.improve.Models.Note;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.FirebaseDatabaseManager;
import dev.danielholmberg.improve.Models.VipImage;
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
                notifyItemChanged(position);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                return oldItem.getTitle().trim().equals(newItem.getTitle().trim())
                        && oldItem.getInfo().trim().equals(newItem.getInfo().trim())
                        && (oldItem.isStared() == newItem.isStared())
                        && (oldItem.hasImage() && oldItem.getVipImages().equals(newItem.getVipImages()))
                        && oldItem.getTags().equals(newItem.getTags());
            }

            @Override
            public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemInserted(position);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRemoved(position);
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

                    if(addedNote.hasImage()) {
                        for(String imageId : addedNote.getVipImages()) {

                            File cachedImage = new File(Improve.getInstance().getImageDir(),
                                    imageId + FirebaseStorageManager.VIP_IMAGE_SUFFIX);

                            if(cachedImage.exists()) {
                                Log.d(TAG, "Image for Note: " + addedNote.getId() +
                                        " exists in Local Filesystem with image id: " + imageId);
                            } else {
                                Log.d(TAG, "Downloading image from Firebase for Note: " + addedNote.getId()
                                        + " with image id: " + imageId);

                                app.getFirebaseStorageManager().downloadImageToLocalFile(imageId, new FirebaseStorageCallback() {
                                    @Override
                                    public void onSuccess(Object file) {}

                                    @Override
                                    public void onFailure(String errorMessage) {}

                                    @Override
                                    public void onProgress(int progress) {}
                                });
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // This method is triggered when the data at a child location has changed.
                Note updatedNote = dataSnapshot.getValue(Note.class);

                if(updatedNote != null) {
                    Note existingNote = (Note) getHashMap().get(updatedNote.getId());
                    if(existingNote == null) {
                        notes.add(updatedNote);
                    } else {
                        notes.updateItemAt(getNotesList().indexOf(existingNote), updatedNote);
                    }

                    Toast.makeText(app, "Note updated", Toast.LENGTH_SHORT).show();

                    app.getMainActivityRef().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                             notifyDataSetChanged();
                        }
                    });
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
