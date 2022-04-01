package com.example.workplan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.workplan.Adapter.NewMeetingEmployeesAdapter;
import com.example.workplan.Model.EmployeeModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewMeetingActivity extends AppCompatActivity {

    // global variable declarations
    private RecyclerView recyclerView;
    private Button createMeeting;
    private EditText meetName;
    private AutoCompleteTextView employeeName;
    private TextView selectDate, selectTime;
    private NewMeetingEmployeesAdapter adapter;
    private String currentName, dueDate, dueTime, userID, tempUserID;
    private int status;

    // lists
    private List<EmployeeModel> eList;
    private List<EmployeeModel> allEmployeeList;
    private List<String> employeeNames;
    private List<String> currentEmployeeInviteNames;
    private List<String> employeeInviteIDs;

    // firebase
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meeting);

        // declare XML elements
        createMeeting = findViewById(R.id.tasksEditTasks); // create meeting button
        employeeName = findViewById(R.id.enterEmployee); // edit text for entering employees
        selectDate = findViewById(R.id.selectDate);
        selectTime = findViewById(R.id.selectTime);
        meetName = findViewById(R.id.itemName); // edit text meeting name
        status = 0; // meeting status

        // declare firebase auth and store
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // array lists
        employeeInviteIDs = new ArrayList<>(); // id's of INVITED employees
        eList = new ArrayList<>(); // current employees on the invite list (for recycler view)
        allEmployeeList = new ArrayList<>(); // ALL employees (maybe not necessary)
        employeeNames = new ArrayList<>(); // ALL employee names
        currentEmployeeInviteNames = new ArrayList<>(); // currently invited employee names

        userID = fAuth.getCurrentUser().getUid();

        // declare recyclerview for meeting invites
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(NewMeetingActivity.this, LinearLayoutManager.VERTICAL,false));
        adapter = new NewMeetingEmployeesAdapter(NewMeetingActivity.this, eList);

        // auto fill employee names adapter
        ArrayAdapter<String> autoFillAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, employeeNames);
        employeeName.setAdapter(autoFillAdapter);

        // set current due date and due time
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);
        SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy");
        dueDate = format.format(calendar.getTime());
        SimpleDateFormat formatTime = new SimpleDateFormat("h:mm aa");
        dueTime = formatTime.format(calendar.getTime());

        // set text to show default date
        calendar.set(YEAR, MONTH, DATE);
        SimpleDateFormat formatDate = new SimpleDateFormat("MMMM dd, yyyy");
        String formattedDate = formatDate.format(calendar.getTime());
        selectDate.setText(formattedDate);

        // set text to show default time
        SimpleDateFormat formatTimeDisp = new SimpleDateFormat("h:mm aa");
        String formattedTime = formatTimeDisp.format(calendar.getTime());
        selectTime.setText(formattedTime);

        // set text to open date dialog
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDateSelect();
            }
        });

        // set text to open time dialog
        selectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTimeSelect();
            }
        });

        // set create meeting to attempt to create meeting
        createMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewMeeting();
            }
        });

        // sets adapter for recyclerView to show current employee invites
        recyclerView.setAdapter(adapter);
        showEmployees();

        // fills list of all employees to set autofill for all names
        fStore.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (DocumentSnapshot document : task.getResult()){
                        String id = document.getId();
                        EmployeeModel employeeModel = document.toObject(EmployeeModel.class).withID(id);
                        allEmployeeList.add(employeeModel);
                        String fullName = employeeModel.getfName() + " " + employeeModel.getlName();
                        employeeNames.add(fullName);
                    }
                }
            }
        });

        // get current user's name
        DocumentReference userDetails = fStore.collection("users").document(userID);
        userDetails.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String fName = documentSnapshot.getString("fName");
                String lName = documentSnapshot.getString("lName");
                currentName = fName + " " + lName;
                Log.i("name", currentName);
            }
        });

    }

    // shows currently invited employees in the recyclerView
    private void showEmployees(){
        fStore.collection("meetingInvites").document(userID).collection("invites").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        String id = documentChange.getDocument().getId();
                        EmployeeModel employeeModel = documentChange.getDocument().toObject(EmployeeModel.class).withID(id);
                        eList.add(employeeModel);
                        //String fullName = employeeModel.getfName() + " " + employeeModel.getlName();
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    // adds employee name to invite list
    public void enterEmployee(View view){
        String enteredName = employeeName.getText().toString();
        // fills array list with current invited employee names to check for duplicates
        currentEmployeeInviteNames.clear();
        for (int i = 0; i < eList.size(); i++){
            String tempfName = eList.get(i).getfName();
            String templName = eList.get(i).getlName();
            String tempFullName = tempfName + " " + templName;
            currentEmployeeInviteNames.add(tempFullName);
        }
        // error cases
        if (enteredName.isEmpty()){ // if no employee name is entered
            Toast.makeText(NewMeetingActivity.this, "Empty Employee Name", Toast.LENGTH_SHORT).show();
        }
        else if (currentEmployeeInviteNames.contains(enteredName)){ // if the name has already been entered
            Toast.makeText(NewMeetingActivity.this, "Employee has already been entered", Toast.LENGTH_SHORT).show();
        }
        else if (currentName.equals(enteredName)){ // if they try to enter their own employee name
            Toast.makeText(NewMeetingActivity.this, "Please invite another employee", Toast.LENGTH_SHORT).show();
        }
        else if (!employeeNames.contains(enteredName)){ // if the employee is invalid
            Toast.makeText(NewMeetingActivity.this, "Invalid Employee", Toast.LENGTH_SHORT).show();
        }
        else {
            String [] split = enteredName.split(String.valueOf(' '));
            // queries based on first and last name to pull a user ID
            fStore.collection("users").whereEqualTo("fName", split[0]).whereEqualTo("lName", split[1]).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()){
                            // gets current user ID
                            tempUserID = (String) document.get("userID");
                            Map<String,Object> userMap = new HashMap<>();

                            // employee invite data
                            userMap.put("fName", split[0]);
                            userMap.put("lName", split[1]);
                            userMap.put("userID", tempUserID);

                            DocumentReference userInvRef = fStore.collection("meetingInvites").document(userID).collection("invites").document(tempUserID);
                            userInvRef.set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(NewMeetingActivity.this, "Employee Added", Toast.LENGTH_SHORT).show();
                                    employeeName.setText("");
                                }
                            });
                        }
                    }
                }
            });

        }
    }

    // To change the Due Date
    private void handleDateSelect(){
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar formatDate = Calendar.getInstance();
                formatDate.set(year, month, dayOfMonth);
                SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy");
                String formattedDate = format.format(formatDate.getTime());
                double compared = formatDate.compareTo(calendar);
                if (compared < 0){
                    Toast.makeText(NewMeetingActivity.this, "Please enter a valid date", Toast.LENGTH_SHORT).show();
                }
                else{
                    selectDate.setText(formattedDate);
                    // Save updated due date to variable
                    dueDate = format.format(formatDate.getTime());
                }
            }
        }, YEAR, MONTH, DATE);

        datePickerDialog.show();

    }

    // To change the Due Time
    private void handleTimeSelect(){
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR);
        int MINUTE = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar formatTime = Calendar.getInstance();
                formatTime.set(Calendar.HOUR, hourOfDay);
                formatTime.set(Calendar.MINUTE, minute);
                SimpleDateFormat format = new SimpleDateFormat("h:mm aa");
                String formattedTime = format.format(formatTime.getTime());
                double compared = formatTime.compareTo(calendar);
                selectTime.setText(formattedTime);
                // Save updated due time to variable
                //SimpleDateFormat dueTimeFormat = new SimpleDateFormat("kk:mm");
                dueTime = format.format(formatTime.getTime());
            }
        }, HOUR, MINUTE, false);

        timePickerDialog.show();
    }

    // create meeting handler
    private void createNewMeeting (){
        // get meeting name from editText field
        String name = meetName.getText().toString();
        // checks if meeting name is empty
        if (name.isEmpty()){
            Toast.makeText(NewMeetingActivity.this, "Empty Meeting Name Field", Toast.LENGTH_SHORT).show();
        }
        else{
            // puts current employee invite IDs and acceptances into array list
            employeeInviteIDs.add(userID+"1");
             for (int i = 0; i < eList.size(); i++){
                employeeInviteIDs.add(eList.get(i).getUserID()+ "0");
             }

             // saves meeting info to a meeting map
             userID = fAuth.getCurrentUser().getUid();
             Map<String,Object> meetMap = new HashMap<>();

             // meeting data
             meetMap.put("name", name);
             meetMap.put("date", dueDate);
             meetMap.put("time", dueTime);
             meetMap.put("complete", status);
             meetMap.put("invited", employeeInviteIDs);
             meetMap.put("by", userID);

            Task<DocumentReference> tasks = fStore.collection("meetings").add(meetMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    Toast.makeText(NewMeetingActivity.this, "Meeting Created", Toast.LENGTH_SHORT).show();
                    int loopSize = eList.size();
                    for (int i = 0; i < loopSize; i++){
                        fStore.collection("meetingInvites").document(userID).collection("invites").document(String.valueOf(eList.get(i).getUserID())).delete();
                    }
                    startActivity(new Intent(NewMeetingActivity.this, ManagerSchedulerActivity.class));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NewMeetingActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}