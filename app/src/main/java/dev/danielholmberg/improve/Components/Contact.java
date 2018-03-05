package dev.danielholmberg.improve.Components;

import java.io.Serializable;

/**
 * Created by DanielHolmberg on 2018-01-17.
 */

public class Contact implements Serializable{
    private String cid;
    private String name;
    private String company;
    private String email;
    private String mobile;

    public Contact() {

    };

    public Contact(String cid, String name, String company, String email, String mobile) {
        this.cid = cid;
        this.name = name;
        this.company = company;
        this.email = email;
        this.mobile = mobile;
    }

    public String getCID() {
        return this.cid;
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

    public String getMobile() {
        return this.mobile;
    }

    public void setCID(String cid) {
        this.cid = cid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) { this.mobile = mobile; }
}