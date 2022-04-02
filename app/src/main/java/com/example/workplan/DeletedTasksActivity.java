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
import com.example.workplan.Adapter.DeletedTasksAdapter;
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
import java.util.List;

public class DeletedTasksActivity extends AppCompatActivity {

    // global variable declarations
    private DeletedTasksAdapter deletedTasksAdapter;
    private RecyclerView recyclerView;
    private List<TaskModel> tList;
    private String userID, acceptedCheck;
    private TextView topText;
    private View notifsBtn, tasksBtn, homeBtn; // menu and sign out buttons

    // firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted_tasks);

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
        recyclerView = findViewById(R.id.homeCurrentTasks);
        tList = new ArrayList<>(); // task list
        deletedTasksAdapter = new DeletedTasksAdapter(DeletedTasksActivity.this, tList);

        // Initialize recyclerView for meetings
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(DeletedTasksActivity.this, LinearLayoutManager.VERTICAL,false));

        recyclerView.setAdapter(deletedTasksAdapter);
        showTasks();

        // Nav to Home
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DeletedTasksActivity.this, HomeActivity.class);
                startActivity(i);
            }
        });

        // Nav to Notifications
        notifsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DeletedTasksActivity.this, NotificationsActivity.class);
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
                            i = new Intent(DeletedTasksActivity.this, ManagerSchedulerActivity.class);
                        }
                        else {
                            i = new Intent(DeletedTasksActivity.this, SchedulerActivity.class);
                        }
                        startActivity(i);
                    }
                });
            }
        });
    }

    // shows current tasks in the recyclerView
    private void showTasks(){
        userID = fAuth.getCurrentUser().getUid();
        // queries based on accepted, incomplete tasks for the current user
        fStore.collection("tasks").whereEqualTo("for",userID).whereEqualTo("accepted", 1).whereEqualTo("complete", 1).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // adds each task model in this query to tList, and the adapter is updated
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        String id = documentChange.getDocument().getId();
                        TaskModel taskModel = documentChange.getDocument().toObject(TaskModel.class).withId(String.valueOf(id));
                        tList.add(taskModel);
                        deletedTasksAdapter.notifyDataSetChanged();
                        if (tList.size() == 0) {
                            topText.setText("You have no recently completed tasks.");
                        }
                    }
                }
            }
        });
    }
}