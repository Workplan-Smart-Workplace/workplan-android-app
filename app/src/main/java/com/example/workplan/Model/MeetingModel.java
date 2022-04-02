package com.example.workplan.Model;

import java.util.List;

public class MeetingModel extends MeetingID{
    private String name, by, date, time;
    private int colour;
    private List<String> invited;

    public int getColour() {
        return colour;
    }

    public String getName() {
        return name;
    }

    public String getBy() {
        return by;
    }

    public List<String> getInvited() {
        return invited;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
