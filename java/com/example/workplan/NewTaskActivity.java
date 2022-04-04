package com.example.workplan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NewTaskActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // global variable declarations
    private Spinner tPrioritySelect;
    private EditText taskName;
    private TextView selectDate, selectTime;
    public Button createTaskBtn;
    public int priority, status, accepted;
    public String userID, dueDate, dueTime;

    // firebase
    public FirebaseFirestore fStore;
    public FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        // declare XML elements
        taskName = findViewById(R.id.itemName);
        selectDate = findViewById(R.id.selectDate);
        selectTime = findViewById(R.id.selectTime);
        createTaskBtn = findViewById(R.id.tasksEditTasks);
        tPrioritySelect = findViewById(R.id.selectPriority);

        // adapter for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(NewTaskActivity.this, R.array.priorities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tPrioritySelect.setAdapter(adapter);
        tPrioritySelect.setOnItemSelectedListener(this);

        // firebase auth and store
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // declare status and accepted as 0 and 1 by default
        status = 0; // whether task is completed
        accepted = 1; // whether task is accepted

        // create default date for task
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        // format due date and declare default
        SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy");
        dueDate = format.format(calendar.getTime());

        // format due time and declare default
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

        // set task button to attempt to create task
        createTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewTask();
            }
        });

        // error case
        //  disable new task button if nothing is in the task name field
        taskName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().equals("")){
                    createTaskBtn.setEnabled(false);
                }
                else{
                    createTaskBtn.setEnabled(true);
                }
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")){
                    createTaskBtn.setEnabled(false);
                }
                else{
                    createTaskBtn.setEnabled(true);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")){
                    createTaskBtn.setEnabled(false);
                }
                else{
                    createTaskBtn.setEnabled(true);
                }
            }
        });

    }

    // open date picker dialog
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
                selectDate.setText(formattedDate);
                // Save updated due date to variable
                //SimpleDateFormat dueDateFormat = new SimpleDateFormat("MM-dd-YYYY");
                dueDate = format.format(formatDate.getTime());
            }
        }, YEAR, MONTH, DATE);

        datePickerDialog.show();

    }

    // open time picker dialog
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
                selectTime.setText(formattedTime);
                // Save updated due time to variable
                //SimpleDateFormat dueTimeFormat = new SimpleDateFormat("kk:mm");
                dueTime = format.format(formatTime.getTime());
            }
        }, HOUR, MINUTE, false);

        timePickerDialog.show();
    }

    // handle spinner if new option is selected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        if (text.equals("High")){
            priority = 2;
        }
        else if (text.equals("Medium")){
            priority = 1;
        }
        else {
            priority = 0;
        }
    }

    // set priority to low by default
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        priority = 0;
    }

    // create new task button handler
    private void createNewTask(){
        // get task name from editText field
        String name = taskName.getText().toString();
        // checks if name is empty
        if (name.isEmpty()){
            Toast.makeText(NewTaskActivity.this, "Empty Task Name Field", Toast.LENGTH_SHORT).show();
        }
        else{
            // saves task data into a hashmap
            userID = fAuth.getCurrentUser().getUid();
            Map<String,Object> taskMap = new HashMap<>();

            // task data
            taskMap.put("task", name);
            taskMap.put("accepted", accepted);
            taskMap.put("date", dueDate);
            taskMap.put("time", dueTime);
            taskMap.put("complete", status);
            taskMap.put("priority", priority);
            taskMap.put("for", userID);
            taskMap.put("by", userID);

            // saves task data into a new document in the tasks collection
            Task<DocumentReference> tasks = fStore.collection("tasks").add(taskMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    Toast.makeText(NewTaskActivity.this, "Task Created", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(NewTaskActivity.this, SchedulerActivity.class));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NewTaskActivity.this, "Task Creation Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}