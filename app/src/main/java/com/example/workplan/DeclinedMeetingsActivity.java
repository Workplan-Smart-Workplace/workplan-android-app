package com.example.workplan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.workplan.Adapter.DeclinedMeetingsAdapter;
import com.example.workplan.Adapter.TaskAdapter;
import com.example.workplan.Model.MeetingModel;
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
import java.util.List;

public class DeclinedMeetingsActivity extends AppCompatActivity {

    // global variable declarations
    private DeclinedMeetingsAdapter declinedMeetingsAdapter;
    private RecyclerView recyclerView;
    private List<MeetingModel> mList;
    private String userID, acceptedCheck;
    private TextView topText;
    private View notifsBtn, tasksBtn, homeBtn; // menu and sign out buttons

    // firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_declined_meetings);

        // bottom buttons declaration + signOut
        notifsBtn = findViewById(R.id.notifsIcon);
        tasksBtn = findViewById(R.id.tasksIcon);
        homeBtn = findViewById(R.id.homeIcon);

        // declare XML elements
        topText = findViewById(R.id.regFlavorText);

        // declare firebase auth and store
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // declare meetings recyclerView
        recyclerView = findViewById(R.id.homeCurrentMeetings);
        mList = new ArrayList<>(); // task list
        declinedMeetingsAdapter = new DeclinedMeetingsAdapter(DeclinedMeetingsActivity.this, mList);

        // Initialize recyclerView for meetings
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(DeclinedMeetingsActivity.this, LinearLayoutManager.VERTICAL,false));

        recyclerView.setAdapter(declinedMeetingsAdapter);
        showMeetings();

        // Nav to Home
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DeclinedMeetingsActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });

        // Nav to Notifications
        notifsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DeclinedMeetingsActivity.this, NotificationsActivity.class);
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
                            i = new Intent(DeclinedMeetingsActivity.this, ManagerSchedulerActivity.class);
                        }
                        else {
                            i = new Intent(DeclinedMeetingsActivity.this, SchedulerActivity.class);
                        }
                        startActivity(i);
                    }
                });
            }
        });
    }

    private void showMeetings(){
        userID = fAuth.getCurrentUser().getUid();
        acceptedCheck = userID + "2";
        fStore.collection("meetings").whereArrayContains("invited", acceptedCheck).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        String id = documentChange.getDocument().getId();
                        MeetingModel meetingModel = documentChange.getDocument().toObject(MeetingModel.class).withId(String.valueOf(id));
                        mList.add(meetingModel);
                        declinedMeetingsAdapter.notifyDataSetChanged();
                        if (mList.size() == 0) {
                            topText.setText("You have no declined meetings.");
                        }
                    }
                }
            }
        });
    }
}