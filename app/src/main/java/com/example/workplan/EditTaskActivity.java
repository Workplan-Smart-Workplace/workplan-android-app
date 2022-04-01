package com.example.workplan;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EditTaskActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    // global variable declarations
    private Spinner tPrioritySelect;
    private EditText taskName;
    private TextView selectDate, selectTime;
    public Button editTaskBtn;
    public int priority, status, accepted;
    public String dueDate, dueTime, taskID, textTaskName;

    // firebase
    public FirebaseFirestore fStore;
    public FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // declare XML elements
        taskName = findViewById(R.id.itemName);
        selectDate = findViewById(R.id.selectDate);
        selectTime = findViewById(R.id.selectTime);
        editTaskBtn = findViewById(R.id.tasksEditTasks);

        // firebase auth and store
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // declare status and accepted as 0 and 1 by default
        status = 0;
        accepted = 1;

        // get task info from recycler adapter
        Bundle extras = getIntent().getExtras();
        taskID = extras.getString("id");
        textTaskName = extras.getString("name");
        dueDate = extras.getString("date");
        dueTime = extras.getString("time");
        priority = extras.getInt("priority");

        // Adapter for Spinner
        tPrioritySelect = findViewById(R.id.selectPriority);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(EditTaskActivity.this, R.array.priorities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tPrioritySelect.setAdapter(adapter);
        tPrioritySelect.setOnItemSelectedListener(this);
        // set priority to previously accepted priority
        tPrioritySelect.setSelection(priority);

        // set text according to task information
        taskName.setText(textTaskName);
        selectDate.setText(dueDate);
        selectTime.setText(dueTime);

        // set date and time to already selected date and time
        selectDate.setText(dueDate);
        selectTime.setText(dueTime);

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

        // set edit button to edit task
        editTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTask();
            }
        });

        //  Disables Edit Task Button if there is nothing in the task name field
        taskName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().equals("")){
                    editTaskBtn.setEnabled(false);
                }
                else{
                    editTaskBtn.setEnabled(true);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")){
                    editTaskBtn.setEnabled(false);
                }
                else{
                    editTaskBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")){
                    editTaskBtn.setEnabled(false);
                }
                else{
                    editTaskBtn.setEnabled(true);
                }
            }
        });
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
                selectDate.setText(formattedDate);
                // Save updated due date to variable
                //SimpleDateFormat dueDateFormat = new SimpleDateFormat("MM-dd-YYYY");
                dueDate = format.format(formatDate.getTime());
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

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    // edit task button handler
    private void editTask (){
        // get task name from EditText field
        String name = taskName.getText().toString();
        // checks if name is empty
        if (name.isEmpty()){
            Toast.makeText(EditTaskActivity.this, "Empty Task Name Field", Toast.LENGTH_SHORT).show();
        }
        // updates task with given information
        fStore.collection("tasks").document(String.valueOf(taskID)).update("task", name);
        fStore.collection("tasks").document(String.valueOf(taskID)).update("date", dueDate);
        fStore.collection("tasks").document(String.valueOf(taskID)).update("time", dueTime);
        fStore.collection("tasks").document(String.valueOf(taskID)).update("priority", priority);

        // return to home activity upon completion
        Toast.makeText(EditTaskActivity.this, "Task Edited", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(EditTaskActivity.this, HomeActivity.class));
    }
}