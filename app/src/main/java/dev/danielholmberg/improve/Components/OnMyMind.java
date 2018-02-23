package dev.danielholmberg.improve.Components;

import java.io.Serializable;

/**
 * Created by DanielHolmberg on 2018-01-21.
 */

public class OnMyMind implements Serializable{
    private String id;
    private String title;
    private String info;

    public OnMyMind() {}

    public OnMyMind(String id, String title, String info) {
        this.id = id;
        this.title = title;
        this.info = info;
    }

    public String getId() {
        return this.id;
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
}
