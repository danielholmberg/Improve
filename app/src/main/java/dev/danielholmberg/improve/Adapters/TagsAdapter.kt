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

import java.util.HashMap;

import dev.danielholmberg.improve.Models.Note;
import dev.danielholmberg.improve.Models.Tag;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.Managers.DatabaseManager;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.ViewHolders.TagViewHolder;

public class TagsAdapter extends RecyclerView.Adapter<TagViewHolder>{
    private static final String TAG = TagsAdapter.class.getSimpleName();

    private Improve app;
    private DatabaseManager databaseManager;
    private SortedList<Tag> tags;
    private Note currentNote;
    private View tagView;
    private boolean editMode = false;

    public TagsAdapter() {
        this.app = Improve.getInstance();
        this.databaseManager = app.getDatabaseManager();

        tags = new SortedList<>(Tag.class, new SortedList.Callback<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.getId().compareTo(o2.getId());
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Tag oldItem, Tag newItem) {
                return oldItem.getLabel().equals(newItem.getLabel())
                        && oldItem.getColor().equals(newItem.getColor())
                        && oldItem.getTextColor().equals(newItem.getTextColor());
            }

            @Override
            public boolean areItemsTheSame(Tag oldItem, Tag newItem) {
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
     * Downloads all tags from the Notes-node and adds a childEventListener to detect changes.
     */
    private void initDatabaseListener() {
        databaseManager.getTagRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // This method is triggered when a new child is added
                // to the location to which this listener was added.

                Log.d(TAG, "Tag OnChildAdded()");

                Tag addedTag = dataSnapshot.getValue(Tag.class);

                if(addedTag != null) {
                    tags.add(addedTag);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // This method is triggered when the data at a child location has changed.

                // TODO - Not used at the moment, useful when user can edit Tags.

                Log.d(TAG, "Tag OnChildChanged()");

                Tag updatedTag = dataSnapshot.getValue(Tag.class);

                if(updatedTag != null) {
                    tags.add(updatedTag);
                    Toast.makeText(app, "Tag updated", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // This method is triggered when a child is removed from the location
                // to which this listener was added.

                // TODO - Not used at the moment, useful when user can edit Tags.

                Log.d(TAG, "Tag OnChildRemoved()");

                final Tag removedTag = dataSnapshot.getValue(Tag.class);

                if(removedTag != null) {
                    tags.remove(removedTag);
                } else {
                    Toast.makeText(app, "Failed to delete tag, please try again later",
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

                Log.e(TAG, "Tags ChildEventListener cancelled: " + databaseError);
            }
        });
    }

    public HashMap<String, Object> getHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        for(int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            hashMap.put(tag.getId(), tag);
        }
        return hashMap;
    }

    public Tag getTag(String tagId) {
        return (Tag) getHashMap().get(tagId);
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void setCurrentNote(Note currentNote) {
        this.currentNote = currentNote;
    }

    public void removeCurrentNote() {
        this.currentNote = null;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        tagView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag, parent, false);

        return new TagViewHolder(tagView);

    }

    @Override
    public void onBindViewHolder(@NonNull final TagViewHolder holder, int position) {
        final Tag tag = this.tags.get(position);

        holder.bindModelToView(tag);

        holder.setEditMode(editMode);

        if(currentNote != null) {
            tagView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(currentNote.containsTag(tag.getId())) {
                        currentNote.removeTag(tag.getId());
                        holder.setTagStatusOnNote(false);
                    } else {
                        currentNote.addTag(tag.getId());
                        holder.setTagStatusOnNote(true);
                    }
                }
            });

            if(currentNote.containsTag(tag.getId())) {
                holder.setTagStatusOnNote(true);
            } else {
                holder.setTagStatusOnNote(false);
            }
        }
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        notifyDataSetChanged();
    }
}
