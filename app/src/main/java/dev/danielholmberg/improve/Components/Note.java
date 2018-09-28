package dev.danielholmberg.improve.Components;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by DanielHolmberg on 2018-01-21.
 */

public class Note implements Serializable {
    private String id;
    private String title;
    private String info;
    private String timestampAdded;
    private String timestampUpdated;
    private boolean archived;
    private String tagId;

    public Note() {}

    public Note(String id, String title, String info, String timestampAdded, String tagId) {
        this.id = id;
        this.title = title;
        this.info = info;
        this.timestampAdded = timestampAdded;
        this.tagId = tagId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setTimestampAdded(String timestampAdded) {
        this.timestampAdded = timestampAdded;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getInfo() {
        return this.info;
    }

    public String getTimestampAdded() {
        return this.timestampAdded;
    }

    public boolean getArchived(){
        return this.archived;
    }

    public String getTimestampUpdated() {
        return timestampUpdated;
    }

    public void setTimestampUpdated(String timestampUpdated) {
        this.timestampUpdated = timestampUpdated;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String toJSON() throws JSONException {
        JSONObject noteObject = new JSONObject();
        JSONObject noteData = new JSONObject();

        noteData.put("archived", this.archived);
        noteData.put("id", this.id);
        noteData.put("info", this.info);
        noteData.put("timestampAdded", this.timestampAdded);
        noteData.put("timestampUpdated", this.timestampUpdated);
        noteData.put("title", this.title);
        noteData.put("tagId", this.tagId);

        noteObject.put(this.id, noteData);

        return noteObject.toString();
    }
}
