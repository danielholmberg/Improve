package dev.danielholmberg.improve.Components;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

/**
 * Created by DanielHolmberg on 2018-01-21.
 */

public class Note implements Serializable{
    private String id;
    private String title;
    private String info;
    private String color;
    private String timestamp;
    private boolean archived;

    public Note() {}

    public Note(String id, String title, String info, String color, String timestamp) {
        this.id = id;
        this.title = title;
        this.info = info;
        this.color = color;
        this.timestamp = timestamp;
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

    public void setColor(String color) {
        this.color = color;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public String getColor() {
        return this.color;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public boolean getArchived(){
        return this.archived;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return archived == note.archived &&
                Objects.equals(id, note.id) &&
                Objects.equals(title, note.title) &&
                Objects.equals(info, note.info) &&
                Objects.equals(color, note.color) &&
                Objects.equals(timestamp, note.timestamp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, title, info, color, timestamp, archived);
    }
}
