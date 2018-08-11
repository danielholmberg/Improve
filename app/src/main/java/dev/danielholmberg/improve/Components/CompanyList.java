package dev.danielholmberg.improve.Components;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class CompanyList extends ExpandableGroup<Contact> {

    public CompanyList(String companyName, List<Contact> contacts) {
        super(companyName, contacts);
    }

}
