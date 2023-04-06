package com.stav.completenotes.utils;

public class Item {
    private String title;
    private String detail;
    private Boolean is_done;//check box
    private String dateTime;
    // private String location;
    private int id;//unique id which we use as requestCode for alarm

    public Item(String title, String detail, Boolean is_done, String dateTime, int id){
        this.title = title;
        this.detail = detail;
        this.is_done = is_done;
        this.dateTime = dateTime;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Boolean getIs_done() {
        return is_done;
    }

    public void setIs_done(Boolean is_done) {
        this.is_done = is_done;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
