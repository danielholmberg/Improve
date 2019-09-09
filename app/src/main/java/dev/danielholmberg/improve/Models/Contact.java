package dev.danielholmberg.improve.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Created by DanielHolmberg on 2018-01-17.
 */

public class Contact implements Parcelable {

    private String id;
    private String name;
    private String companyId;
    private String email = "";
    private String phone = "";
    private String comment = "";
    private String timestampAdded;
    private String timestampUpdated;

    public Contact() {}

    public Contact(String id, String name, String companyId, String email, String phone, String comment, String timestampAdded) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.email = email;
        this.phone = phone;
        this.comment = comment;
        this.timestampAdded = timestampAdded;
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

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestampAdded() {
        return timestampAdded;
    }

    public void setTimestampAdded(String timestampAdded) {
        this.timestampAdded = timestampAdded;
    }

    public String getTimestampUpdated() {
        return timestampUpdated;
    }

    public void setTimestampUpdated(String timestampUpdated) {
        this.timestampUpdated = timestampUpdated;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(companyId);
        parcel.writeString(email);
        parcel.writeString(phone);
        parcel.writeString(comment);
        parcel.writeString(timestampAdded);
        parcel.writeString(timestampUpdated);
    }

    protected Contact(Parcel in) {
        id = in.readString();
        name = in.readString();
        companyId = in.readString();
        email = in.readString();
        phone = in.readString();
        comment = in.readString();
        timestampAdded = in.readString();
        timestampUpdated = in.readString();
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return id.equals(contact.id) &&
                Objects.equals(name, contact.name) &&
                Objects.equals(companyId, contact.companyId) &&
                Objects.equals(email, contact.email) &&
                Objects.equals(phone, contact.phone) &&
                Objects.equals(comment, contact.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, companyId, email, phone, comment);
    }
}