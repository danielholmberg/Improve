package dev.danielholmberg.improve.Components;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by DanielHolmberg on 2018-01-17.
 */

public class Contact implements Parcelable {
    private String id;
    private String name;
    private String company;
    private String email = "";
    private String phone = "";
    private String comment = "";
    private String color;

    public Contact() {}

    public Contact(String id, String name, String company, String email, String phone, String comment, String color) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.email = email;
        this.phone = phone;
        this.comment = comment;
        this.color = color;
    }

    protected Contact(Parcel in) {
        id = in.readString();
        name = in.readString();
        company = in.readString();
        email = in.readString();
        phone = in.readString();
        comment = in.readString();
        color = in.readString();
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

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setEmail(String email) {
        if(email != null) {
            this.email = email;
        }
    }

    public void setPhone(String phone) {
        if(phone != null) {
            this.phone = phone;
        }
    }

    public void setComment(String comment) {
        if(comment != null) {
            this.comment = comment;
        }
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCompany() {
        return this.company;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getComment() {
        return this.comment;
    }

    public String getColor() {
        return this.color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(company);
        parcel.writeString(email);
        parcel.writeString(phone);
        parcel.writeString(comment);
        parcel.writeString(color);
    }
}