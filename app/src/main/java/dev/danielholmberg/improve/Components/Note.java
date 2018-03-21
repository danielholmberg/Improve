package dev.danielholmberg.improve.Components;

import java.io.Serializable;

/**
 * Created by DanielHolmberg on 2018-01-21.
 */

public class Note implements Serializable{
    private String id;
    private String title;
    private String info;
    private String color;
    private String timestamp;
    private boolean isDone;

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

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return title;
    }

    public String getInfo() {
        return info;
    }

    public String getColor() {
        return this.color;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean getIsDone(){
        return isDone;
    }
 }
