package dev.danielholmberg.improve.Components;

import java.io.Serializable;

public class Tag implements Serializable {

    private String tagId;
    private String label;
    private String colorHex;
    private int colorInt;

    public Tag() {}

    public Tag(String tagId, String label, String colorHex, int colorInt) {
        this.tagId = tagId;
        this.label = label;
        this.colorHex = colorHex;
        this.colorInt = colorInt;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String color) {
        this.colorHex = color;
    }

    public int getColorInt() {
        return colorInt;
    }

    public void setColorInt(int colorInt) {
        this.colorInt = colorInt;
    }
}
