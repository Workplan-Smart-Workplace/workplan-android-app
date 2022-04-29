package com.example.workplan.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workplan.Model.EmployeeModel;
import com.example.workplan.Model.TaskModel;
import com.example.workplan.NewMeetingActivity;
import com.example.workplan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NewMeetingEmployeesAdapter extends RecyclerView.Adapter<NewMeetingEmployeesAdapter.EmployeesViewHolder> {

    // global variable declarations
    private List<EmployeeModel> employeeList;
    private NewMeetingActivity activity;
    private FirebaseFirestore firestore;
    private FirebaseAuth fAuth;

    public NewMeetingEmployeesAdapter(NewMeetingActivity meetingActivity, List<EmployeeModel> employeeList){
        this.employeeList = employeeList;
        activity = meetingActivity;
    }

    @NonNull
    @Override
    public EmployeesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.meeting_employees, parent, false);
        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        return new EmployeesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeesViewHolder holder, int position) {
        EmployeeModel employeeModel = employeeList.get(position);
        String fullName = employeeModel.getfName() + " " + employeeModel.getlName();
        holder.name.setText(fullName);
        holder.deleteClickListener.setPosition(position);
    }

    private class DeleteClickListener implements View.OnClickListener {
        private int mPosition;
        public void setPosition(int position) {
            mPosition = position;
        }
        @Override
        public void onClick (View v) {
            EmployeeModel employeeModel = employeeList.get(mPosition);
            deleteEmployee(employeeModel, mPosition);
        }
    }

    public void deleteEmployee(EmployeeModel employee, int position){
        String userID = fAuth.getCurrentUser().getUid();
        firestore.collection("meetingInvites").document(userID).collection("invites").document(String.valueOf(employee.getUserID())).delete();
        employeeList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    public class EmployeesViewHolder extends RecyclerView.ViewHolder{
        DeleteClickListener deleteClickListener;
        TextView name;
        View delete;
        public EmployeesViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.employeeName);
            delete = itemView.findViewById(R.id.deleteTaskX);
            deleteClickListener = new DeleteClickListener();
            delete.setOnClickListener(deleteClickListener);
        }
    }


}
