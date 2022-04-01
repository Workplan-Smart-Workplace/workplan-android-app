package com.example.workplan.Model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class MeetingID {
    @Exclude
    public String MeetingID;
    public <T extends MeetingID> T withId(@NonNull final String id){
        this.MeetingID = id;
        return (T)this;
    }
}
