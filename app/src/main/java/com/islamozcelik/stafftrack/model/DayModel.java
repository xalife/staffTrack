package com.islamozcelik.stafftrack.model;

import java.util.Date;

public class DayModel {
    private String date;
    private String time;

    public DayModel(){

    }
    public DayModel(String date,String time){
        this.date = date;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Date StringtoDate(){
        return new Date(date);
    }
}
