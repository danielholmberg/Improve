package dev.danielholmberg.improve.Components;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class Company extends ExpandableGroup<Contact> {

    private String color;

    public Company(String companyName, List<Contact> contacts) {
        super(companyName, contacts);
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return this.color;
    }
}
