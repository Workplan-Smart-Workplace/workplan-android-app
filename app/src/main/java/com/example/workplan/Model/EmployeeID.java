package com.example.workplan.Model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class EmployeeID {
    @Exclude
    public String EmployeeId;

    public <T extends EmployeeID> T withID(@NonNull final String id){
        this.EmployeeId = id;
        return(T) this;
    }
}
