package com.example.workplan.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.workplan.Adapter.AcceptedMeetingsAdapter;
import com.example.workplan.Adapter.TaskAdapter;
import com.example.workplan.AppController;
import com.example.workplan.Constant;
import com.example.workplan.HomeActivity;
import com.example.workplan.HomeActivitywithFragments;
import com.example.workplan.Model.MeetingModel;
import com.example.workplan.Model.TaskModel;
import com.example.workplan.R;
import com.example.workplan.location.LocationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;



/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment  implements LocationUtils.MyLocation {




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


    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
// declare firebase auth and store
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // declare XML elements
        hWelcome = view.findViewById(R.id.screenTitle);
        weatherDegrees=view.findViewById(R.id.weatherDegrees);
        weatherTitle=view.findViewById(R.id.weatherTitle);
        weatherDesc=view.findViewById(R.id.weatherDesc);
        hDate = view.findViewById(R.id.regFlavorText);
        tasksDesc = view.findViewById(R.id.tasksDesc);
        meetingsDesc = view.findViewById(R.id.meetingsDesc);

        // declare tasks recyclerView
        taskRecyclerView = view.findViewById(R.id.homeCurrentTasks);
        tList = new ArrayList<>(); // task list
        taskAdapter = new TaskAdapter(getActivity(), tList);

        meetingRecyclerView = view.findViewById(R.id.homeCurrentMeetings);
        mList = new ArrayList<>();
        acceptedMeetingsAdapter = new AcceptedMeetingsAdapter(getActivity(), mList);
        // Initialize recyclerView for tasks
        taskRecyclerView.setHasFixedSize(true);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));

        // Initialize recyclerView for meetings
        meetingRecyclerView.setHasFixedSize(true);
        meetingRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));


        getLocation();

        // sets adapter for recyclerView and shows tasks
        taskRecyclerView.setAdapter(taskAdapter);
        showTasks();

        // sets adapter for recyclerView and shows meetings
        meetingRecyclerView.setAdapter(acceptedMeetingsAdapter);
        showMeetings();
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
                        else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                            String id = documentChange.getDocument().getId();
                            TaskModel taskModel = documentChange.getDocument().toObject(TaskModel.class);
                            taskModel.TaskId=(id);
                            for(Iterator<TaskModel> iterator = tList.iterator(); iterator.hasNext(); ) {
                                if(iterator.next().TaskId.equals(id))
                                    iterator.remove();
                            }
                            tList.add(taskModel);
                            taskAdapter.notifyDataSetChanged();
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
        if (!hasPermissions(getActivity(), PERMISSIONSLoc)) {
            requestPermissions(PERMISSIONSLoc, PERMISSION_Location);
        } else {
            locationUtils = new LocationUtils(getActivity(), this, false);
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
            Volley.newRequestQueue(AppController.getInstance()).add(new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
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