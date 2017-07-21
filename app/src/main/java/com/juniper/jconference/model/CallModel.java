package com.juniper.jconference.model;

import java.util.ArrayList;

/**
 * Created by koteswara on 29/06/17.
 */

public class CallModel {
    private String title;
    private ArrayList<String> numberList;
    private String conference;
    private String date;
    private String time;
    private String timezone;
    public String getPhNumber() {
        return phNumber;
    }

    public void setPhNumber(String phNumber) {
        this.phNumber = phNumber;
    }

    private String phNumber;

    public String getDateandtime() {
        return dateandtime;
    }

    public void setDateandtime(String dateandtime) {
        this.dateandtime = dateandtime;
    }

    private String dateandtime;
    public ArrayList<String> getNumberList() {
        return numberList;
    }
    public void setNumberList(ArrayList<String> numberList) {
        this.numberList = numberList;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getConference() {
        return conference;
    }

    public void setConference(String conference) {
        this.conference = conference;
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

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }


}
