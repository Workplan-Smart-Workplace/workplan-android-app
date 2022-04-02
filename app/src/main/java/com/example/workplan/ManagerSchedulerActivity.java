package com.example.workplan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ManagerSchedulerActivity extends AppCompatActivity {

    // global variable declarations
    public Button tNewTaskBtn, tNewMeetingBtn;
    private View tSignOutBtn, tNotifsButton, tHomeButton;
    private TextView tDate;

    // firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_scheduler);

        // bottom buttons declaration / sign out
        tNotifsButton = findViewById(R.id.notifsIcon);
        tHomeButton = findViewById(R.id.homeIcon);
        tSignOutBtn = findViewById(R.id.signOutBtn);

        // declare XML elements
        tNewTaskBtn = findViewById(R.id.newTask);
        tNewMeetingBtn = findViewById(R.id.newMeeting);
        tDate = findViewById(R.id.regFlavorText);

        // firebase auth and store
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // sets date to current date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatDate = new SimpleDateFormat("EEE, MMM d");
        String formattedDate = formatDate.format(calendar.getTime());
        tDate.setText(formattedDate);

        // sets function for sign out button
        tSignOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        // nav to notifications
        tNotifsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ManagerSchedulerActivity.this, NotificationsActivity.class);
                startActivity(i);
            }
        });

        // nav to home
        tHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ManagerSchedulerActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });
    }

    // signs out current user
    private void signOut(){
        fAuth.signOut();
        Toast.makeText(ManagerSchedulerActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(ManagerSchedulerActivity.this, SignInActivity.class);
        startActivity(i);
    }

    // new task onClick (XML) from the new task Button
    public void newTask (View view){
        Intent i = new Intent(ManagerSchedulerActivity.this, NewTaskActivity.class);
        startActivity(i);
    }

    // new meeting onClick (XML) from the new task Button
    public void newMeeting (View view){
        Intent i = new Intent(ManagerSchedulerActivity.this, NewMeetingActivity.class);
        startActivity(i);
    }

    public void navToDeletedTasks (View view){
        Intent i = new Intent(ManagerSchedulerActivity.this, DeletedTasksActivity.class);
        startActivity(i);
    }

    public void navToDeclinedMeetings (View view){
        Intent i = new Intent(ManagerSchedulerActivity.this, DeclinedMeetingsActivity.class);
        startActivity(i);
    }
}