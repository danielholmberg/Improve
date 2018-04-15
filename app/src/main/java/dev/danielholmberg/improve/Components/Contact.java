package dev.danielholmberg.improve.Components;

import java.io.Serializable;

/**
 * Created by DanielHolmberg on 2018-01-17.
 */

public class Contact implements Serializable{
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

}