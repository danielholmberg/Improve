package dev.danielholmberg.improve.Components;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class Company extends ExpandableGroup<Contact> {

    public Company(String companyName, List<Contact> contacts) {
        super(companyName, contacts);
    }

}
