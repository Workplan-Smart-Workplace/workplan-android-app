package com.example.workplan.Model;

import java.text.SimpleDateFormat;

public class TaskModel extends TaskId{

    private String task, date, time;
    private int priority;

    public String getTask() {
        return task;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getPriority() {
        return priority;
    }
}
