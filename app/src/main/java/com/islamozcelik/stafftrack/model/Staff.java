package com.islamozcelik.stafftrack.model;

public class Staff {
    private String userid;

    public Staff(){

    }
    public Staff(String userid){
        this.userid = userid;
    }
    public String getUserid() {
        System.out.println("userid from staff:"+userid);
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
