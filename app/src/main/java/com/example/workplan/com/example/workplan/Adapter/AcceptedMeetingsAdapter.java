package com.example.workplan.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workplan.HomeActivity;
import com.example.workplan.Model.MeetingModel;
import com.example.workplan.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AcceptedMeetingsAdapter extends RecyclerView.Adapter<AcceptedMeetingsAdapter.AcceptedMeetingViewHolder> {

    // global variable declarations
    private List<MeetingModel> meetingList;
    private List<String> employeeID;
    private Activity activity;
    private FirebaseFirestore firestore;
    private FirebaseAuth fAuth;
    private Context context;
    private String userID;

    public AcceptedMeetingsAdapter(Activity homeActivity, List<MeetingModel> meetingList){
        this.meetingList = meetingList;
        activity = homeActivity;
    }

    @NonNull
    @Override
    public AcceptedMeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.accpted_meetings, parent, false );
        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        return new AcceptedMeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AcceptedMeetingViewHolder holder, int position) {

        // meeting colours
        int secondaryTeal = Color.parseColor("#40B5AE");
        int secondaryGreen = Color.parseColor("#A5CC36");
        int primaryIndigo = Color.parseColor("#4E37DA");
        int secondaryOrange = Color.parseColor("#DA9937");

        // set position for onClick listeners for each meeting
        holder.denyClickListener.setPosition(position);

        // set meeting information on xml
        MeetingModel meetingModel = meetingList.get(position);
        holder.mName.setText(meetingModel.getName());
        holder.mDueDate.setText(meetingModel.getDate() + ", ");
        holder.mDueTime.setText(meetingModel.getTime());
        int mColour = meetingModel.getColour();

        switch(mColour){
            case 0:
                holder.mMeetingColour.setBackgroundColor(secondaryTeal);
                break;
            case 1:
                holder.mMeetingColour.setBackgroundColor(secondaryGreen);
                break;
            case 2:
                holder.mMeetingColour.setBackgroundColor(primaryIndigo);
                break;
            case 3:
                holder.mMeetingColour.setBackgroundColor(secondaryOrange);
                break;
        }

        DocumentReference userDetails = firestore.collection("users").document(meetingModel.getBy());
        userDetails.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String fName = documentSnapshot.getString("fName");
                holder.mEmployees.setText(fName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    public void denyMeeting(MeetingModel meeting, int position) {
        ArrayList<String> currentInvited = (ArrayList<String>) meeting.getInvited();
        ArrayList <String> newInvited = (ArrayList<String>) meeting.getInvited();
        newInvited.set(currentInvited.indexOf(userID+"1"), userID+"2");
        firestore.collection("meetings").document(String.valueOf(meeting.MeetingID)).update("invited", newInvited);
        meetingList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    // creates onclick listener for edit
    private class DenyClickListener implements View.OnClickListener {
        private int mPosition;
        public void setPosition(int position) {
            mPosition = position;
        }
        @Override
        public void onClick (View v) {
            MeetingModel meetingModel = meetingList.get(mPosition);
            denyMeeting(meetingModel, mPosition);
        }
    }

    public class AcceptedMeetingViewHolder extends RecyclerView.ViewHolder{

        // declare XML elements and onClick Listeners
        AcceptedMeetingsAdapter.DenyClickListener denyClickListener;
        TextView mName, mDueDate, mDueTime, mEmployees;
        View mConfirm, mDeny;
        ConstraintLayout mMeetingColour;

        public AcceptedMeetingViewHolder(@NonNull View itemView) {
            super(itemView);

            context = itemView.getContext();
            mName = itemView.findViewById(R.id.name);
            mDueDate = itemView.findViewById(R.id.dueDate);
            mDueTime = itemView.findViewById(R.id.dueTime);
            mEmployees = itemView.findViewById(R.id.membersNames);
            mConfirm = itemView.findViewById(R.id.singleTaskCheckIcon);
            mDeny = itemView.findViewById(R.id.singleTaskChangeIcon);
            mMeetingColour = itemView.findViewById(R.id.cardBackground);

            denyClickListener = new AcceptedMeetingsAdapter.DenyClickListener();
            mDeny.setOnClickListener(denyClickListener);
        }
    }
}
