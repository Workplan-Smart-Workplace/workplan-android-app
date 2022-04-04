package com.example.workplan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.workplan.Adapter.AcceptedMeetingsAdapter;
import com.example.workplan.Adapter.TaskAdapter;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // global variable declarations
    private View signOutBtn, notifsBtn, tasksBtn; // menu and sign out buttons
    private TaskAdapter taskAdapter; // adapter for recycler view
    private AcceptedMeetingsAdapter acceptedMeetingsAdapter; // adapter for meetings
    private List<TaskModel> tList; // task model list
    private List<MeetingModel> mList; // meeting model list
    private RecyclerView taskRecyclerView, meetingRecyclerView;
    private String userID, acceptedCheck;
    private TextView hWelcome, hDate, tasksDesc, meetingsDesc;
    private ListenerRegistration meetingRegistration, taskRegistration;

    // firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // bottom buttons declaration + signOut
        signOutBtn = findViewById(R.id.signOut);
        notifsBtn = findViewById(R.id.notifsIcon);
        tasksBtn = findViewById(R.id.tasksIcon);

        // declare firebase auth and store
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        
        // declare XML elements
        hWelcome = findViewById(R.id.screenTitle);
        hDate = findViewById(R.id.regFlavorText);
        tasksDesc = findViewById(R.id.tasksDesc);
        meetingsDesc = findViewById(R.id.meetingsDesc);

        // declare tasks recyclerView
        taskRecyclerView = findViewById(R.id.homeCurrentTasks);
        tList = new ArrayList<>(); // task list
        taskAdapter = new TaskAdapter(HomeActivity.this, tList);

        meetingRecyclerView = findViewById(R.id.homeCurrentMeetings);
        mList = new ArrayList<>();
        acceptedMeetingsAdapter = new AcceptedMeetingsAdapter(HomeActivity.this, mList);

        // Sets Date text to current Date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatDate = new SimpleDateFormat("EEE, MMM d");
        String formattedDate = formatDate.format(calendar.getTime());
        hDate.setText(formattedDate);

        // Sets welcome message to user's first name
        userID = fAuth.getCurrentUser().getUid(); // sets current user ID
        DocumentReference userDetails = fStore.collection("users").document(userID);
        userDetails.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String fName = documentSnapshot.getString("fName");
                hWelcome.setText("Welcome, " + fName); // sets user title text to user's first name
            }
        });

        // Initialize recyclerView for tasks
        taskRecyclerView.setHasFixedSize(true);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.VERTICAL,false));

        // Initialize recyclerView for meetings
        meetingRecyclerView.setHasFixedSize(true);
        meetingRecyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this, LinearLayoutManager.VERTICAL,false));

        // Sets function for Sign Out
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        // Nav to Notifications
        notifsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskRegistration.remove();
                meetingRegistration.remove();
                Intent i = new Intent(HomeActivity.this, NotificationsActivity.class);
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
                        taskRegistration.remove();
                        meetingRegistration.remove();
                        String isManager = documentSnapshot.getString("manager");
                        Intent i;
                        if (isManager.equals("true")) { // if user is a manager
                            i = new Intent(HomeActivity.this, ManagerSchedulerActivity.class);
                        }
                        else {
                            i = new Intent(HomeActivity.this, SchedulerActivity.class);
                        }
                        startActivity(i);
                    }
                });
            }
        });

        // sets adapter for recyclerView and shows tasks
        taskRecyclerView.setAdapter(taskAdapter);
        showTasks();

        // sets adapter for recyclerView and shows meetings
        meetingRecyclerView.setAdapter(acceptedMeetingsAdapter);
        showMeetings();

    }

    // Override going back to Sign In screen upon back click
    @Override
    public void onBackPressed(){
        // Maybe have signOut here
    }

    // signs out current user
    private void signOut(){
        taskRegistration.remove();
        meetingRegistration.remove();
        fAuth.signOut();
        Toast.makeText(HomeActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(HomeActivity.this, SignInActivity.class);
        startActivity(i);
    }

    // shows current tasks in the recyclerView
    private void showTasks(){
        userID = fAuth.getCurrentUser().getUid();
        // queries based on accepted, incomplete tasks for the current user
        taskRegistration = fStore.collection("tasks").whereEqualTo("for",userID).whereEqualTo("accepted", 1).whereEqualTo("complete", 0).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // adds each task model in this query to tList, and the adapter is updated
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        String id = documentChange.getDocument().getId();
                        TaskModel taskModel = documentChange.getDocument().toObject(TaskModel.class).withId(String.valueOf(id));
                        tList.add(taskModel);
                        taskAdapter.notifyDataSetChanged();
                        tasksDesc.setText("Current Tasks (" + tList.size() + ")");
                        if (tList.size() == 0) {
                            tasksDesc.setText("You have no current tasks.");
                        }
                    }
                }
            }
        });
    }
    private void showMeetings(){
        userID = fAuth.getCurrentUser().getUid();
        acceptedCheck = userID + "1";
        meetingRegistration = fStore.collection("meetings").whereArrayContains("invited", acceptedCheck).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        String id = documentChange.getDocument().getId();
                        MeetingModel meetingModel = documentChange.getDocument().toObject(MeetingModel.class).withId(String.valueOf(id));
                        mList.add(meetingModel);
                        acceptedMeetingsAdapter.notifyDataSetChanged();
                        meetingsDesc.setText("Upcoming Meetings (" + mList.size() + ")");
                        if (mList.size() == 0) {
                            meetingsDesc.setText("You have no upcoming meetings.");
                        }
                    }
                }
            }
        });
    }

}