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

public class SchedulerActivity extends AppCompatActivity {

    // global variable declarations
    private View tNotifsButton, tHomeButton; // menu and sign out buttons
    private TextView tDate;
    public Button tNewTaskBtn;

    // firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);

        // bottom buttons declaration + SignOut
        tNotifsButton = findViewById(R.id.notifsIcon);
        tHomeButton = findViewById(R.id.homeIcon);

        // declare XML elements
        tNewTaskBtn = findViewById(R.id.newTask);
        tDate = findViewById(R.id.regFlavorText);

        // declare firebase auth and store
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // sets date text to current date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatDate = new SimpleDateFormat("EEE, MMM d");
        String formattedDate = formatDate.format(calendar.getTime());
        tDate.setText(formattedDate);

        // nav to notifications
        tNotifsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SchedulerActivity.this, NotificationsActivity.class);
                startActivity(i);
                finish();
            }
        });

        // nav to home
        tHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    // nav to new task activity
    public void newTask (View view){
        Intent i = new Intent(SchedulerActivity.this, NewTaskActivity.class);
        startActivity(i);
    }

    public void navToDeletedTasks (View view){
        Intent i = new Intent(SchedulerActivity.this, DeletedTasksActivity.class);
        startActivity(i);
    }

    public void navToDeclinedMeetings (View view){
        Intent i = new Intent(SchedulerActivity.this, DeclinedMeetingsActivity.class);
        startActivity(i);
    }

}