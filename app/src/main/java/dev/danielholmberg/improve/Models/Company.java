package dev.danielholmberg.improve.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Objects;

public class Company implements Parcelable{

    private String id;
    private String name;
    private HashMap contactsList;

    public Company() {}

    public Company(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Company(String id, String name, HashMap<String, Object> contacts) {
        this.id = id;
        this.name = name;
        this.contactsList = contacts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Object> getContacts() {
        return contactsList;
    }

    public void setContacts(HashMap<String, Object> contactsList) {
        this.contactsList = contactsList;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeMap(contactsList);
    }

    protected Company(Parcel in) {
        id = in.readString();
        name = in.readString();
        contactsList = in.readHashMap(HashMap.class.getClassLoader());
    }

    public static final Creator<Company> CREATOR = new Creator<Company>() {
        @Override
        public Company createFromParcel(Parcel in) {
            return new Company(in);
        }

        @Override
        public Company[] newArray(int size) {
            return new Company[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return id.equals(company.id) &&
                Objects.equals(name, company.name) &&
                Objects.equals(contactsList, company.contactsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, contactsList);
    }
}
