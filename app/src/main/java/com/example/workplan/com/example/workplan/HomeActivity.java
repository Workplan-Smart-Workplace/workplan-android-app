package com.example.workplan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.workplan.Adapter.AcceptedMeetingsAdapter;
import com.example.workplan.Adapter.TaskAdapter;
import com.example.workplan.Model.EmployeeModel;
import com.example.workplan.Model.MeetingModel;
import com.example.workplan.Model.TaskModel;
import com.example.workplan.location.LocationUpdatesService;
import com.example.workplan.location.LocationUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements LocationUtils.MyLocation {

    // global variable declarations
    private View signOutBtn, notifsBtn, tasksBtn; // menu and sign out buttons
    private TaskAdapter taskAdapter; // adapter for recycler view
    private AcceptedMeetingsAdapter acceptedMeetingsAdapter; // adapter for meetings
    private List<TaskModel> tList; // task model list
    private List<MeetingModel> mList; // meeting model list
    private RecyclerView taskRecyclerView, meetingRecyclerView;
    private String userID, acceptedCheck;
    private TextView hWelcome,weatherDegrees,weatherTitle,weatherDesc, hDate, tasksDesc, meetingsDesc;
    private ListenerRegistration meetingRegistration, taskRegistration;

    // firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    /// used to get user current location and fetch weather data
    private double latitude = 0;
    private double longitude = 0;
    private int  PERMISSION_Location = 2;
    private LocationUtils locationUtils;

    // permission required to used gps for the user

    private String[] PERMISSIONSLoc = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };















    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // topic to subscribe for notificaion
        FirebaseMessaging.getInstance().subscribeToTopic("allusers");



        // bottom buttons declaration + signOut
        signOutBtn = findViewById(R.id.signOut);
        notifsBtn = findViewById(R.id.notifsIcon);
        tasksBtn = findViewById(R.id.tasksIcon);

        // declare firebase auth and store
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        
        // declare XML elements
        hWelcome = findViewById(R.id.screenTitle);
        weatherDegrees=findViewById(R.id.weatherDegrees);
        weatherTitle=findViewById(R.id.weatherTitle);
        weatherDesc=findViewById(R.id.weatherDesc);
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
        /// check if user id is not null then fetch the data from firestore
        if(userID != null) {


            DocumentReference userDetails = fStore.collection("users").document(userID);
            userDetails.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    try {

                        EmployeeModel employeeModel = documentSnapshot.toObject(EmployeeModel.class);
                        hWelcome.setText("Welcome, " + employeeModel.getfName()); // sets user title text to user's first name

                        /// if current employee is manger then we subscribe topic manger in order to send notification to the users
//                        if (employeeModel.isManger()) {
//                            FirebaseMessaging.getInstance().subscribeToTopic("manger");
//                        }
                    } catch (Exception e) {
                        Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

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

                        if(documentSnapshot != null) {
                            EmployeeModel employeeModel = documentSnapshot.toObject(EmployeeModel.class);
                            Intent i;
//                            if (employeeModel.isManger()) { // if user is a manager
//                                i = new Intent(HomeActivity.this, ManagerSchedulerActivity.class);
//                            } else {
//                                i = new Intent(HomeActivity.this, SchedulerActivity.class);
//                            }
                           // startActivity(i);
                        }
                    }
                });
            }
        });

        getLocation();

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

        try {
            taskRegistration.remove();
            meetingRegistration.remove();
            if(Session.isManger(HomeActivity.this))
            FirebaseMessaging.getInstance().unsubscribeFromTopic("manager");
            Session.clearUserSession(HomeActivity.this);
            fAuth.signOut();

        }
        catch (Exception e){

        }
        Toast.makeText(HomeActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(HomeActivity.this, SignInActivity.class);
        startActivity(i);
        finish();
    }

    // shows current tasks in the recyclerView
    private void showTasks(){
        userID = fAuth.getCurrentUser().getUid();
        // queries based on accepted, incomplete tasks for the current user
        taskRegistration = fStore.collection("tasks").whereEqualTo("for",userID).whereEqualTo("accepted", 1).whereEqualTo("complete", 0).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                // adds each task model in this query to tList, and the adapter is updated
                if(value !=null) {
                    for (DocumentChange documentChange : value.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
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
            }
        });
    }
    private void showMeetings(){
        userID = fAuth.getCurrentUser().getUid();
        acceptedCheck = userID + "1";
        meetingRegistration = fStore.collection("meetings").whereArrayContains("invited", acceptedCheck).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value != null) {
                    for (DocumentChange documentChange : value.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
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
            }
        });
    }


    private void getLocation() {
        if (!hasPermissions(this, PERMISSIONSLoc)) {
            requestPermissions(PERMISSIONSLoc, PERMISSION_Location);
        } else {
            locationUtils = new LocationUtils(this, this, false);
        }
    }
    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_Location && PackageManager.PERMISSION_GRANTED == grantResults[0])
            getLocation();

    }



    @Override
    public void locationUpdates(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            String url = String.format("https://api.openweathermap.org/data/2.5/weather?lat=%1$s&lon=%2$s&appid=%3$s&units=%4$s",
                    latitude,
                    longitude,
                    Constant.WeatherAPIKEY,
                    "metric");
            Volley.newRequestQueue(HomeActivity.this).add(new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
                public void onResponse(String str) {
                    try {

                        JSONObject jsonObject = new JSONObject(String.valueOf(str));
                        weatherTitle.setText(jsonObject.getJSONArray("weather").getJSONObject(0).getString("main"));
                        weatherDesc.setText(jsonObject.getJSONArray("weather").getJSONObject(0).getString("description").toString());
                        weatherDegrees.setText(jsonObject.getJSONObject("main").getString("temp").toString()+ "\u00B0");

                    } catch (Exception e2) {
                        Log.e("Error ct", e2.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e("GridLayout", "Volley:", volleyError);
                }
            } ));

        }

    }


}