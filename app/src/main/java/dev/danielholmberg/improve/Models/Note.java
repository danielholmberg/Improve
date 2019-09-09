package dev.danielholmberg.improve.Models;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

import dev.danielholmberg.improve.Fragments.NoteDetailsDialogFragment;
import dev.danielholmberg.improve.Improve;
import dev.danielholmberg.improve.R;
import dev.danielholmberg.improve.ViewHolders.TagViewHolder;

/**
 * Created by DanielHolmberg on 2018-01-21.
 */

@IgnoreExtraProperties
public class Note implements Parcelable {

    private String id;
    private String title;
    private String info;
    private String added;
    private String updated;
    private boolean archived = false;
    private HashMap<String, Boolean> tags = new HashMap<>();

    public Note() {}

    public Note(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getAdded() {
        return added;
    }

    public void setAdded(String added) {
        this.added = added;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public boolean getArchived() {
        return this.archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public HashMap<String, Boolean> getTags() {
        return this.tags;
    }

    public void setTags(HashMap<String, Boolean> tags) {
        // Do not add if Tag has been removed.
        for(String tagId: tags.keySet()) {
            if(Improve.getInstance().getTagsAdapter().getTag(tagId) == null) {
                tags.remove(tagId);
            }
        }
        this.tags = tags;
    }

    @Exclude
    public boolean isArchived() {
        return getArchived();
    }

    @Exclude
    public String toJSON() throws JSONException {
        JSONObject noteObject = new JSONObject();
        JSONObject noteData = new JSONObject();

        noteData.put("archived", this.archived);
        noteData.put("info", this.info);
        noteData.put("added", this.added);
        noteData.put("updated", this.updated);
        noteData.put("title", this.title);

        if(!this.tags.isEmpty()) noteData.put("tags", tags);

        noteObject.put(this.id, noteData);

        return noteObject.toString();
    }

    @Exclude
    public void addTag(String tagId) {
        this.tags.put(tagId, true);
    }

    @Exclude
    public void removeTag(String tagId) {
        this.tags.remove(tagId);
    }

    @Exclude
    public boolean containsTag(String id) {
        return tags.keySet().contains(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(info);
        parcel.writeInt((archived ? 1 : 0));
        parcel.writeString(added);
        parcel.writeString(updated);
        parcel.writeMap(tags);
    }

    protected Note(Parcel in) {
        id = in.readString();
        title = in.readString();
        info = in.readString();
        archived = in.readInt() != 0;
        added = in.readString();
        updated = in.readString();
        in.readMap(tags, HashMap.class.getClassLoader());
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return archived == note.archived &&
                id.equals(note.id) &&
                Objects.equals(title, note.title) &&
                Objects.equals(info, note.info) &&
                Objects.equals(added, note.added) &&
                Objects.equals(updated, note.updated) &&
                Objects.equals(tags, note.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, info, added, updated, archived, tags);
    }
}
