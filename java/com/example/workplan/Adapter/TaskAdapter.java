package com.example.workplan.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workplan.EditTaskActivity;
import com.example.workplan.HomeActivity;
import com.example.workplan.Model.TaskModel;
import com.example.workplan.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    // global variable declarations
    private List<TaskModel> taskList;
    private HomeActivity activity;
    private FirebaseFirestore firestore;
    private Context context;

    public TaskAdapter(HomeActivity homeActivity, List<TaskModel> taskList){
        this.taskList = taskList;
        activity = homeActivity;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // set repeated xml as accepted tasks XML
        View view = LayoutInflater.from(activity).inflate(R.layout.accpted_tasks, parent, false );
        firestore = FirebaseFirestore.getInstance();
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        // NOTE: IMPLEMENT OVERDUE DATES HERE

        // task priority colours
        int secondaryTeal = Color.parseColor("#40B5AE");
        int secondaryGreen = Color.parseColor("#A5CC36");
        int primaryIndigo = Color.parseColor("#4E37DA");

        // sets position for onclick listener for each task
        holder.completeClickListener.setPosition(position);
        holder.editClickListener.setPosition(position);

        // set task information on xml
        TaskModel taskModel = taskList.get(position);
        holder.tName.setText(taskModel.getTask());
        holder.tDueDate.setText(taskModel.getDate() + ", ");
        holder.tDueTime.setText(taskModel.getTime());

        // set colour on task depending on priority
        if (taskModel.getPriority() == 0){
            holder.tTaskColour.setBackgroundColor(secondaryGreen);
        }
        if (taskModel.getPriority() == 1){
            holder.tTaskColour.setBackgroundColor(secondaryTeal);
        }
        if (taskModel.getPriority() == 2){
            holder.tTaskColour.setBackgroundColor(primaryIndigo);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // handler for complete task button
    public void completeTask(TaskModel task, int position) {
        firestore.collection("tasks").document(String.valueOf(task.TaskId)).update("complete", 1);
        taskList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    // handler for edit task button
    public void editTask(TaskModel task, int position) {
        // combines task information in intent for EditTaskActivity
        String id = task.TaskId;
        Intent i = new Intent(context, EditTaskActivity.class);
        i.putExtra("id", id);
        i.putExtra("name", task.getTask());
        i.putExtra("date", task.getDate());
        i.putExtra("time", task.getTime());
        i.putExtra("priority", task.getPriority());
        context.startActivity(i);
    }

    // creates onclick listener for complete
    private class CompleteClickListener implements View.OnClickListener {
        private int mPosition;
        public void setPosition(int position) {
            mPosition = position;
        }
        @Override
        public void onClick (View v) {
            TaskModel taskModel = taskList.get(mPosition);
            completeTask(taskModel, mPosition);
        }
    }

    // creates onclick listener for edit
    private class EditClickListener implements View.OnClickListener {
        private int mPosition;
        public void setPosition(int position) {
            mPosition = position;
        }
        @Override
        public void onClick (View v) {
            TaskModel taskModel = taskList.get(mPosition);
            editTask(taskModel, mPosition);
        }
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder{
        // declare XML elements and onclick listeners
        CompleteClickListener completeClickListener;
        EditClickListener editClickListener;
        TextView tName, tDueDate, tDueTime;
        View tEdit, tComplete;
        ConstraintLayout tTaskColour;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            tName = itemView.findViewById(R.id.name);
            tDueDate = itemView.findViewById(R.id.dueDate);
            tDueTime = itemView.findViewById(R.id.dueTime);
            tEdit = itemView.findViewById(R.id.singleTaskChangeIcon);
            tComplete = itemView.findViewById(R.id.singleTaskCheckIcon);
            tTaskColour = itemView.findViewById(R.id.cardBackground);

            completeClickListener = new CompleteClickListener();
            editClickListener = new EditClickListener();

            tComplete.setOnClickListener(completeClickListener);
            tEdit.setOnClickListener(editClickListener);
        }

    }

}
