package dev.danielholmberg.improve.Components;

import java.io.Serializable;

/**
 * Created by DanielHolmberg on 2018-01-21.
 */

public class OnMyMind implements Serializable{
    private String id;
    private String title;
    private String info;
    private String color;
    private String createdTimestamp;
    private String updatedTimestamp;
    private boolean isDone;

    public OnMyMind() {}

    public OnMyMind(String id, String title, String info, String color, String createdTimestamp) {
        this.id = id;
        this.title = title;
        this.info = info;
        this.color = color;
        this.createdTimestamp = createdTimestamp;
    }

    public OnMyMind(String id, String title, String info, String color, String createdTimestamp, String updatedTimestamp) {
        this.id = id;
        this.title = title;
        this.info = info;
        this.color = color;
        this.createdTimestamp = createdTimestamp;
        this.updatedTimestamp = updatedTimestamp;
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

    public void setCreatedTimestamp(String timestamp) {
        this.createdTimestamp = timestamp;
    }

    public void setUpdatedTimestamp(String updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
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

    public String getCreatedTimestamp() {
        return createdTimestamp;
    }

    public String getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public boolean getIsDone(){
        return isDone;
    }
 }
