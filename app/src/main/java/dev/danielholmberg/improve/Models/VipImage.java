package dev.danielholmberg.improve.Models;

import android.net.Uri;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class VipImage {

    private String id;
    private String path;
    private String originalFilePath;

    public VipImage() {}

    public VipImage(String id, String path) {
        this.id = id;
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Exclude
    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }

    @Exclude
    public String getOriginalFilePath() {
        return this.originalFilePath;
    }
}
