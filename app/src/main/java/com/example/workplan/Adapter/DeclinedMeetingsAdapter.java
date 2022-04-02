package com.example.workplan.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workplan.DeclinedMeetingsActivity;
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

public class DeclinedMeetingsAdapter extends RecyclerView.Adapter<DeclinedMeetingsAdapter.DeclinedMeetingViewHolder>{

    // global variable declarations
    private List<MeetingModel> meetingList;
    private List<String> employeeID;
    private DeclinedMeetingsActivity activity;
    private FirebaseFirestore firestore;
    private FirebaseAuth fAuth;
    private Context context;
    private String userID;

    public DeclinedMeetingsAdapter(DeclinedMeetingsActivity declinedMeetingsActivity, List<MeetingModel> meetingList){
        this.meetingList = meetingList;
        activity = declinedMeetingsActivity;
    }

    @NonNull
    @Override
    public DeclinedMeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // set repeated xml as Declined meetings
        View view = LayoutInflater.from(activity).inflate(R.layout.declined_meetings, parent, false );
        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();
        return new DeclinedMeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeclinedMeetingViewHolder holder, int position) {
        // set position for onClick listeners for each meeting
        holder.restoreClickListener.setPosition(position);
        holder.deleteClickListener.setPosition(position);

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

    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    // handler for confirm meeting button
    public void restoreMeeting(MeetingModel meeting, int position) {
        ArrayList <String> currentInvited = (ArrayList<String>) meeting.getInvited();
        ArrayList <String> newInvited = (ArrayList<String>) meeting.getInvited();
        newInvited.set(currentInvited.indexOf(userID+"2"), userID+"1");
        firestore.collection("meetings").document(String.valueOf(meeting.MeetingID)).update("invited", newInvited);
        meetingList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    // handler for deny meeting button
    public void deleteMeeting(MeetingModel meeting, int position) {
        ArrayList <String> currentInvited = (ArrayList<String>) meeting.getInvited();
        ArrayList <String> newInvited = (ArrayList<String>) meeting.getInvited();
        newInvited.set(currentInvited.indexOf(userID+"2"), userID+"3");
        firestore.collection("meetings").document(String.valueOf(meeting.MeetingID)).update("invited", newInvited);
        meetingList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    // creates onclick listener for complete
    private class RestoreClickListener implements View.OnClickListener {
        private int mPosition;
        public void setPosition(int position) {
            mPosition = position;
        }
        @Override
        public void onClick (View v) {
            MeetingModel meetingModel = meetingList.get(mPosition);
            restoreMeeting(meetingModel, mPosition);
        }
    }

    // creates onclick listener for edit
    private class DeleteClickListener implements View.OnClickListener {
        private int mPosition;
        public void setPosition(int position) {
            mPosition = position;
        }
        @Override
        public void onClick (View v) {
            MeetingModel meetingModel = meetingList.get(mPosition);
            deleteMeeting(meetingModel, mPosition);
        }
    }


    public class DeclinedMeetingViewHolder extends RecyclerView.ViewHolder{
        // declare XML elements and onClick Listeners
        RestoreClickListener restoreClickListener;
        DeleteClickListener deleteClickListener;
        TextView mName, mDueDate, mDueTime, mEmployees;
        View mConfirm, mDeny;
        ConstraintLayout mMeetingColour;

        public DeclinedMeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            mName = itemView.findViewById(R.id.name);
            mDueDate = itemView.findViewById(R.id.dueDate);
            mDueTime = itemView.findViewById(R.id.dueTime);
            mEmployees = itemView.findViewById(R.id.membersNames);
            mConfirm = itemView.findViewById(R.id.singleTaskCheckIcon);
            mDeny = itemView.findViewById(R.id.singleTaskChangeIcon);

            restoreClickListener = new RestoreClickListener();
            deleteClickListener = new DeleteClickListener();

            mConfirm.setOnClickListener(restoreClickListener);
            mDeny.setOnClickListener(deleteClickListener);
        }
    }
}
