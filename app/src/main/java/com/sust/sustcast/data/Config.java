package com.sust.sustcast.data;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Config {

    private String mail_id;
    private String mail_subject;
    private String page_id;
    private String page_link;

    public Config(String mail_id, String mail_subject, String page_id, String page_link) {
        this.mail_id = mail_id;
        this.mail_subject = mail_subject;
        this.page_id = page_id;
        this.page_link = page_link;
    }

    public Config() {

    }

    public String getMail_id() {
        return mail_id;
    }

    public void setMail_id(String mail_id) {
        this.mail_id = mail_id;
    }

    public String getMail_subject() {
        return mail_subject;
    }

    public void setMail_subject(String mail_subject) {
        this.mail_subject = mail_subject;
    }

    public String getPage_id() {
        return page_id;
    }

    public void setPage_id(String page_id) {
        this.page_id = page_id;
    }

    public String getPage_link() {
        return page_link;
    }

    public void setPage_link(String page_link) {
        this.page_link = page_link;
    }
}