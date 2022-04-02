package com.example.workplan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.workplan.Adapter.UnacceptedMeetingsAdapter;
import com.example.workplan.Model.MeetingID;
import com.example.workplan.Model.MeetingModel;
import com.example.workplan.Model.TaskModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    // global variable declarations
    private View homeBtn, tasksBtn, menuSelect, notifsBtn;
    private List<MeetingModel> mList; // meeting model list
    private UnacceptedMeetingsAdapter meetingsAdapter;
    private RecyclerView meetingsRecyclerView;
    private String userID, acceptedCheck;
    private TextView meetDesc;

    // firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // bottom buttons declaration
        homeBtn = findViewById(R.id.homeIcon);
        tasksBtn = findViewById(R.id.tasksIcon);
        notifsBtn = findViewById(R.id.notifsIcon);

        //declare XML elements
        meetDesc = findViewById(R.id.meetReqTitle);
        menuSelect = findViewById(R.id.menuSelect);

        //declare firebase auth and store
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // declare meetings recyclerview
        meetingsRecyclerView = findViewById(R.id.meetingRequests);
        mList = new ArrayList<>(); // meetings list
        meetingsAdapter = new UnacceptedMeetingsAdapter(NotificationsActivity.this, mList);

        meetingsRecyclerView.setHasFixedSize(true);
        meetingsRecyclerView.setLayoutManager(new LinearLayoutManager(NotificationsActivity.this, LinearLayoutManager.VERTICAL, false));
        meetingsRecyclerView.setAdapter(meetingsAdapter);
        showMeetings();

        // Nav to Notifications
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NotificationsActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });

        // Nav to tasks (depending on if manager or not)
        tasksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference userDetails = fStore.collection("users").document(userID);
                userDetails.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String isManager = documentSnapshot.getString("manager");
                        Intent i;
                        if (isManager.equals("true")) { // if user is a manager
                            i = new Intent(NotificationsActivity.this, ManagerSchedulerActivity.class);
                        }
                        else {
                            i = new Intent(NotificationsActivity.this, SchedulerActivity.class);
                        }
                        startActivity(i);
                    }
                });
            }
        });
    }

    private void showMeetings(){
        userID = fAuth.getCurrentUser().getUid();
        acceptedCheck = userID + "0";
        fStore.collection("meetings").whereArrayContains("invited", acceptedCheck).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        String id = documentChange.getDocument().getId();
                        MeetingModel meetingModel = documentChange.getDocument().toObject(MeetingModel.class).withId(String.valueOf(id));
                        mList.add(meetingModel);
                        meetingsAdapter.notifyDataSetChanged();
                        if (mList.isEmpty()) {
                            meetDesc.setText("You have no meeting requests.");
                        }
                    }
                }
            }
        });

    }
}