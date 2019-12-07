package dev.danielholmberg.improve.Models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import dev.danielholmberg.improve.Improve;

/**
 * Created by DanielHolmberg on 2018-01-21.
 */

@IgnoreExtraProperties
public class Note implements Parcelable {

    private String id;
    private String title;
    private String info;
    private boolean stared = false;
    private String added;
    private String updated;
    private boolean archived = false;
    private HashMap<String, Boolean> tags = new HashMap<>();

    // VIP values
    private HashMap<String, String> vipImages = new HashMap<>();

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

    public boolean getStared() {
        return stared;
    }

    public void setStared(boolean stared) {
        this.stared = stared;
    }

    public HashMap<String, String> getVipImages() {
        return this.vipImages;
    }

    public void setVipImages(HashMap<String, String> vipImages) {
        this.vipImages = vipImages;
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

    // Utility functions
    @Exclude
    public boolean hasImage() {
        return this.vipImages.size() > 0;
    }

    @Exclude
    public boolean isStared() { return getStared(); }

    @Exclude
    public boolean isArchived() {
        return getArchived();
    }

    @Exclude
    public String toString() {
        JSONObject noteData = new JSONObject();

        try {
            noteData.put("id", this.id);
            noteData.put("archived", this.archived);
            noteData.put("info", this.info);
            noteData.put("added", this.added);
            noteData.put("updated", this.updated);
            noteData.put("title", this.title);
            noteData.put("stared", this.stared);

            if(this.vipImages.size() > 0) noteData.put("vipImages", new JSONObject(this.vipImages));
            if(!this.tags.isEmpty()) noteData.put("tags", new JSONObject(this.tags));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return noteData.toString();
    }

    @Exclude
    public void addVipImage(VipImage vipImage) {
        this.vipImages.put(vipImage.getId(), vipImage.getPath());
    }

    @Exclude
    public void removeVipImage(String imageId) {
        this.vipImages.remove(imageId);
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
        parcel.writeInt((stared ? 1 : 0));
        parcel.writeInt((archived ? 1 : 0));
        parcel.writeString(added);
        parcel.writeString(updated);
        parcel.writeMap(vipImages);
        parcel.writeMap(tags);
    }

    protected Note(Parcel in) {
        id = in.readString();
        title = in.readString();
        info = in.readString();
        stared = in.readInt() != 0;
        archived = in.readInt() != 0;
        added = in.readString();
        updated = in.readString();
        in.readMap(vipImages, HashMap.class.getClassLoader());
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
                stared == note.stared &&
                id.equals(note.id) &&
                Objects.equals(title, note.title) &&
                Objects.equals(info, note.info) &&
                Objects.equals(added, note.added) &&
                Objects.equals(updated, note.updated) &&
                Objects.equals(vipImages, note.vipImages) &&
                Objects.equals(tags, note.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, info, stared, added, updated, archived, vipImages, tags);
    }
}
