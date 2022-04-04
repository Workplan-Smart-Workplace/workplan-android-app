package com.example.workplan.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workplan.EditTaskActivity;
import com.example.workplan.Model.MeetingModel;
import com.example.workplan.Model.TaskModel;
import com.example.workplan.NotificationsActivity;
import com.example.workplan.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UnacceptedMeetingsAdapter extends RecyclerView.Adapter<UnacceptedMeetingsAdapter.UnacceptedMeetingViewHolder>{

    // global variable declarations
    private List<MeetingModel> meetingList;
    private List<String> employeeID;
    private NotificationsActivity activity;
    private FirebaseFirestore firestore;
    private FirebaseAuth fAuth;
    private Context context;
    private String userID;

    public UnacceptedMeetingsAdapter(NotificationsActivity notificationsActivity, List<MeetingModel> meetingList){
        this.meetingList = meetingList;
        activity = notificationsActivity;
    }

    @NonNull
    @Override
    public UnacceptedMeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // set repeated xml as unaccepted meetings
        View view = LayoutInflater.from(activity).inflate(R.layout.unaccpted_meetings, parent, false );
        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        return new UnacceptedMeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UnacceptedMeetingViewHolder holder, int position) {

        // meeting colours
        int secondaryTeal = Color.parseColor("#40B5AE");
        int secondaryGreen = Color.parseColor("#A5CC36");
        int primaryIndigo = Color.parseColor("#4E37DA");
        int secondaryOrange = Color.parseColor("#DA9937");

        // set position for onClick listeners for each meeting
        holder.confirmClickListener.setPosition(position);
        holder.denyClickListener.setPosition(position);

        // set meeting information on xml
        MeetingModel meetingModel = meetingList.get(position);
        holder.mName.setText(meetingModel.getName());
        holder.mDueDate.setText(meetingModel.getDate() + ", ");
        holder.mDueTime.setText(meetingModel.getTime());

        DocumentReference userDetails = firestore.collection("users").document(meetingModel.getBy());
        userDetails.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String fName = documentSnapshot.getString("fName");
                holder.mEmployees.setText(fName);
            }
        });

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

    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    // handler for confirm meeting button
    public void confirmMeeting(MeetingModel meeting, int position) {
        ArrayList <String> currentInvited = (ArrayList<String>) meeting.getInvited();
        ArrayList <String> newInvited = (ArrayList<String>) meeting.getInvited();
        newInvited.set(currentInvited.indexOf(userID+"0"), userID+"1");
        firestore.collection("meetings").document(String.valueOf(meeting.MeetingID)).update("invited", newInvited);
        meetingList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    // handler for deny meeting button
    public void denyMeeting(MeetingModel meeting, int position) {
        ArrayList <String> currentInvited = (ArrayList<String>) meeting.getInvited();
        ArrayList <String> newInvited = (ArrayList<String>) meeting.getInvited();
        newInvited.set(currentInvited.indexOf(userID+"0"), userID+"2");
        firestore.collection("meetings").document(String.valueOf(meeting.MeetingID)).update("invited", newInvited);
        meetingList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    // creates onclick listener for complete
    private class ConfirmClickListener implements View.OnClickListener {
        private int mPosition;
        public void setPosition(int position) {
            mPosition = position;
        }
        @Override
        public void onClick (View v) {
            MeetingModel meetingModel = meetingList.get(mPosition);
            confirmMeeting(meetingModel, mPosition);
        }
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


    public class UnacceptedMeetingViewHolder extends RecyclerView.ViewHolder{
        // declare XML elements and onClick Listeners
        ConfirmClickListener confirmClickListener;
        DenyClickListener denyClickListener;
        TextView mName, mDueDate, mDueTime, mEmployees;
        View mConfirm, mDeny;
        ConstraintLayout mMeetingColour;

        public UnacceptedMeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            mName = itemView.findViewById(R.id.name);
            mDueDate = itemView.findViewById(R.id.dueDate);
            mDueTime = itemView.findViewById(R.id.dueTime);
            mEmployees = itemView.findViewById(R.id.membersNames);
            mConfirm = itemView.findViewById(R.id.singleTaskCheckIcon);
            mDeny = itemView.findViewById(R.id.singleTaskChangeIcon);
            mMeetingColour = itemView.findViewById(R.id.cardBackground);

            confirmClickListener = new ConfirmClickListener();
            denyClickListener = new DenyClickListener();

            mConfirm.setOnClickListener(confirmClickListener);
            mDeny.setOnClickListener(denyClickListener);
        }
    }
}
