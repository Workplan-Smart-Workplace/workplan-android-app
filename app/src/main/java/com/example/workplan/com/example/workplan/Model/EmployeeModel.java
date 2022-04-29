package com.example.workplan.Model;

import com.google.firebase.firestore.PropertyName;

public class EmployeeModel extends EmployeeID {

    private String fName, lName, userID;


    private boolean manager;

    @PropertyName(value="manager")
    public boolean getisManger() {
        return manager;
    }
    @PropertyName(value="manager")
    public void setisManger(boolean manager) {
        this.manager = manager;
    }

    public String getfName() {
        return fName;
    }

    public String getlName() {
        return lName;
    }

    public String getUserID() {
        return userID;
    }
}
