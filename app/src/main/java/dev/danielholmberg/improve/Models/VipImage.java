package dev.danielholmberg.improve.Models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class VipImage {

    private String id;
    private String originalFilePath;

    public VipImage() {}

    public VipImage(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
