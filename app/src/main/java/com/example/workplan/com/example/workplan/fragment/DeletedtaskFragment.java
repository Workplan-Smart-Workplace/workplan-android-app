package com.example.workplan.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workplan.Adapter.DeletedTasksAdapter;
import com.example.workplan.DeclinedMeetingsActivity;
import com.example.workplan.DeletedTasksActivity;
import com.example.workplan.Model.TaskModel;
import com.example.workplan.NewMeetingActivity;
import com.example.workplan.NewTaskActivity;
import com.example.workplan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeletedtaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeletedtaskFragment extends Fragment {



    // global variable declarations
    private DeletedTasksAdapter deletedTasksAdapter;
    private RecyclerView recyclerView;
    private List<TaskModel> tList;
    private String userID, acceptedCheck;
    private TextView topText;


    // firebase
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;


    public DeletedtaskFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SchedulerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DeletedtaskFragment newInstance(String param1, String param2) {
        DeletedtaskFragment fragment = new DeletedtaskFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tList = new ArrayList<>();

    }






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_deleted_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // declare firebase auth and store
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // declare meetings recyclerView
        recyclerView = view.findViewById(R.id.homeCurrentTasks);

        deletedTasksAdapter = new DeletedTasksAdapter(getActivity(), tList);

        // Initialize recyclerView for meetings
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));

        recyclerView.setAdapter(deletedTasksAdapter);
        showTasks();



    }





    // shows current tasks in the recyclerView
    private void showTasks(){
        userID = fAuth.getCurrentUser().getUid();
        // queries based on accepted, incomplete tasks for the current user
        fStore.collection("tasks").whereEqualTo("for",userID).whereEqualTo("accepted", 1).whereEqualTo("complete", 1).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value != null) {
                    // adds each task model in this query to tList, and the adapter is updated
                    for (DocumentChange documentChange : value.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
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
            }
        });
    }




}