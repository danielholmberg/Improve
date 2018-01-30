package dev.danielholmberg.improve.Components;

/**
 * Created by DanielHolmberg on 2018-01-17.
 */

public class Contact {
    private String cid;
    private String firstName;
    private String lastName;
    private String company;
    private String email;
    private String phone;

    public Contact() {

    };

    public Contact(String cid, String firstName, String lastName, String company, String email, String phone) {
        this.cid = cid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = company;
        this.email = email;
        this.phone = phone;
    }

    public String getCID() {
        return this.cid;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
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

    public void setCID(String cid) {
        this.cid = cid;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) { this.phone = phone; }
}